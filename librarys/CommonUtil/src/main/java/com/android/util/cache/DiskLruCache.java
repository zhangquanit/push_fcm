/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.util.cache;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * *****************************************************************************
 * Taken from the JB source code, can be found in:
 * libcore/luni/src/main/java/libcore/io/DiskLruCache.java
 * or direct link:
 * https://android.googlesource.com/platform/libcore/+/android-4.1.1_r1/luni/src/main/java/libcore/io/DiskLruCache.java
 * *****************************************************************************
 * <p>
 * A cache that uses a bounded amount of space on a filesystem. Each cache
 * entry has a string key and a fixed number of values. Values are byte
 * sequences, accessible as streams or files. Each value must be between {@code
 * 0} and {@code Integer.MAX_VALUE} bytes in length.
 *
 * <p>The cache stores its data in a directory on the filesystem. This
 * directory must be exclusive to the cache; the cache may delete or overwrite
 * files from its directory. It is an error for multiple processes to use the
 * same cache directory at the same time.
 *
 * <p>This cache limits the number of bytes that it will store on the
 * filesystem. When the number of stored bytes exceeds the limit, the cache will
 * remove entries in the background until the limit is satisfied. The limit is
 * not strict: the cache may temporarily exceed it while waiting for files to be
 * deleted. The limit does not include filesystem overhead or the cache
 * journal so space-sensitive applications should set a conservative limit.
 *
 * <p>Clients call {@link #edit} to create or update the values of an entry. An
 * entry may have only one editor at one time; if a value is not available to be
 * edited then {@link #edit} will return null.
 * <ul>
 * <li>When an entry is being <strong>created</strong> it is necessary to
 * supply a full set of values; the empty value should be used as a
 * placeholder if necessary.
 * <li>When an entry is being <strong>edited</strong>, it is not necessary
 * to supply data for every value; values default to their previous
 * value.
 * </ul>
 * Every {@link #edit} call must be matched by a call to {@link Editor#commit}
 * or {@link Editor#abort}. Committing is atomic: a read observes the full set
 * of values as they were before or after the commit, but never a mix of values.
 *
 * <p>Clients call {@link #get} to read a snapshot of an entry. The read will
 * observe the value at the time that {@link #get} was called. Updates and
 * removals after the call do not impact ongoing reads.
 *
 * <p>This class is tolerant of some I/O errors. If files are missing from the
 * filesystem, the corresponding entries will be dropped from the cache. If
 * an error occurs while writing a cache value, the edit will fail silently.
 * Callers should handle other problems by catching {@code IOException} and
 * responding appropriately.
 */
public final class DiskLruCache implements Closeable {
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TMP = "journal.tmp";
    static final String MAGIC = "libcore.io.DiskLruCache";
    static final String VERSION_1 = "1";
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    private static final String REMOVE = "REMOVE";
    private static final String READ = "READ";

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    /*
     * This cache uses a journal file named "journal". A typical journal file
     * looks like this:
     *     libcore.io.DiskLruCache
     *     1
     *     100
     *     2
     *
     *     CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
     *     DIRTY 335c4c6028171cfddfbaae1a9c313c52
     *     CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
     *     REMOVE 335c4c6028171cfddfbaae1a9c313c52
     *     DIRTY 1ab96a171faeeee38496d8b330771a7a
     *     CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
     *     READ 335c4c6028171cfddfbaae1a9c313c52
     *     READ 3400330d1dfc7f3f7f4b8d4d803dfcf6
     *
     * The first five lines of the journal form its header. They are the
     * constant string "libcore.io.DiskLruCache", the disk cache's version,
     * the application's version, the value count, and a blank line.
     *
     * Each of the subsequent lines in the file is a record of the state of a
     * cache entry. Each line contains space-separated values: a state, a key,
     * and optional state-specific values.
     *   o DIRTY lines track that an entry is actively being created or updated.
     *     Every successful DIRTY action should be followed by a CLEAN or REMOVE
     *     action. DIRTY lines without a matching CLEAN or REMOVE indicate that
     *     temporary files may need to be deleted.
     *   o CLEAN lines track a cache entry that has been successfully published
     *     and may be read. A publish line is followed by the lengths of each of
     *     its values.
     *   o READ lines track accesses for LRU.
     *   o REMOVE lines track entries that have been deleted.
     *
     * The journal file is appended to as cache operations occur. The journal may
     * occasionally be compacted by dropping redundant lines. A temporary file named
     * "journal.tmp" will be used during compaction; that file should be deleted if
     * it exists when the cache is opened.
     */

    private final File mDirectory;
    private final File mJournalFile;
    private final File mJournalFileTmp;
    private final int mAppVersion;
    private final long maxSize;
    private final int mValueCount;
    private long mSize = 0;
    private Writer mJournalWriter;
    private final LinkedHashMap<String, Entry> mLruEntries
            = new LinkedHashMap<String, Entry>(0, 0.75f, true);
    private int mRedundantOpCount;

    /**
     * To differentiate between old and current snapshots, each entry is given
     * a sequence number each time an edit is committed. A snapshot is stale if
     * its sequence number is not equal to its entry's sequence number.
     */
    private long mNextSequenceNumber = 0;

    /* From java.util.Arrays */
    @SuppressWarnings("unchecked")
    private static <T> T[] copyOfRange(T[] original, int start, int end) {
        final int originalLength = original.length; // For exception priority compatibility.
        if (start > end) {
            throw new IllegalArgumentException();
        }
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        final int resultLength = end - start;
        final int copyLength = Math.min(resultLength, originalLength - start);
        final T[] result = (T[]) Array
                .newInstance(original.getClass().getComponentType(), resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    private static final int KBUNIT = 1024;

    /**
     * @param reader r
     * @return the remainder of 'reader' as a string, closing it when done.
     * @throws IOException e
     */
    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[KBUNIT];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
            return writer.toString();
        } finally {
            reader.close();
        }
    }

    private static final int STRINGBUILDERSIZE = 80;

    /**
     * Returns the ASCII characters up to but not including the next "\r\n", or
     * "\n".
     *
     * @param in I
     * @return String
     * @throws java.io.IOException if the stream is exhausted before the next newline
     *                             character.
     */
    public static String readAsciiLine(InputStream in) throws IOException {
        // TODO: support UTF-8 here instead

        StringBuilder result = new StringBuilder(STRINGBUILDERSIZE);
        while (true) {
            int c = in.read();
            if (c == -1) {
                throw new EOFException();
            } else if (c == '\n') {
                break;
            }

            result.append((char) c);
        }
        int length = result.length();
        if (length > 0 && result.charAt(length - 1) == '\r') {
            result.setLength(length - 1);
        }
        return result.toString();
    }

    /**
     * Closes 'closeable', ignoring any checked exceptions. Does nothing if 'closeable' is null.
     *
     * @param closeable c
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * Recursively delete everything in {@code dir}.
     *
     * @param dir file directory
     * @throws java.io.IOException if the stream is exhausted before the next newline
     *                             character.
     */
    // TODO: this should specify paths as Strings rather than as Files
    public static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("not a directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteContents(file);
            }
            if (!file.delete()) {
                throw new IOException("failed to delete file: " + file);
            }
        }
    }

    /**
     * This cache uses a single background thread to evict entries.
     */
    private final ExecutorService mExecutorService = new ThreadPoolExecutor(0, 1,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final Callable<Void> mCleanupCallable = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            synchronized (DiskLruCache.this) {
                if (mJournalWriter == null) {
                    return null; // closed
                }
                trimToSize();
                if (journalRebuildRequired()) {
                    rebuildJournal();
                    mRedundantOpCount = 0;
                }
            }
            return null;
        }
    };

    private DiskLruCache(File directory, int appVersion, int valueCount, long maxsize) {
        this.mDirectory = directory;
        this.mAppVersion = appVersion;
        this.mJournalFile = new File(directory, JOURNAL_FILE);
        this.mJournalFileTmp = new File(directory, JOURNAL_FILE_TMP);
        this.mValueCount = valueCount;
        this.maxSize = maxsize;
    }

    /**
     * Opens the cache in {@code directory}, creating a cache if none exists
     * there.
     *
     * @param directory  a writable directory
     * @param appVersion a
     * @param valueCount the number of values per cache entry. Must be positive.
     * @param maxSize    the maximum number of bytes this cache should use to store
     * @return disklrucache
     * @throws IOException if reading or writing the cache directory fails
     */
    public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize)
            throws IOException {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        if (valueCount <= 0) {
            throw new IllegalArgumentException("valueCount <= 0");
        }

        // prefer to pick up where we left off
        DiskLruCache cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        if (cache.mJournalFile.exists()) {
            try {
                cache.readJournal();
                cache.processJournal();
                cache.mJournalWriter = new BufferedWriter(new FileWriter(cache.mJournalFile, true),
                        IO_BUFFER_SIZE);
                return cache;
            } catch (IOException journalIsCorrupt) {
//                System.logW("DiskLruCache " + directory + " is corrupt: "
//                        + journalIsCorrupt.getMessage() + ", removing");
                cache.delete();
            }
        }

        // create a new empty cache
        directory.mkdirs();
        cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        cache.rebuildJournal();
        return cache;
    }

    private void readJournal() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(mJournalFile), IO_BUFFER_SIZE);
        try {
            String magic = readAsciiLine(in);
            String version = readAsciiLine(in);
            String appVersionString = readAsciiLine(in);
            String valueCountString = readAsciiLine(in);
            String blank = readAsciiLine(in);
            if (!MAGIC.equals(magic)
                    || !VERSION_1.equals(version)
                    || !Integer.toString(mAppVersion).equals(appVersionString)
                    || !Integer.toString(mValueCount).equals(valueCountString)
                    || !"".equals(blank)) {
                throw new IOException("unexpected journal header: ["
                        + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
            }

            while (true) {
                try {
                    readJournalLine(readAsciiLine(in));
                } catch (EOFException endOfJournal) {
                    break;
                }
            }
        } finally {
            closeQuietly(in);
        }
    }

    private void readJournalLine(String line) throws IOException {
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new IOException("unexpected journal line: " + line);
        }

        String key = parts[1];
        if (parts[0].equals(REMOVE) && parts.length == 2) {
            mLruEntries.remove(key);
            return;
        }

        Entry entry = mLruEntries.get(key);
        if (entry == null) {
            entry = new Entry(key);
            mLruEntries.put(key, entry);
        }

        if (parts[0].equals(CLEAN) && parts.length == 2 + mValueCount) {
            entry.mReadable = true;
            entry.mCurrentEditor = null;
            entry.setLengths(copyOfRange(parts, 2, parts.length));
        } else if (parts[0].equals(DIRTY) && parts.length == 2) {
            entry.mCurrentEditor = new Editor(entry);
        } else if (parts[0].equals(READ) && parts.length == 2) {
//        	int a = 1;
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    private void processJournal() throws IOException {
        deleteIfExists(mJournalFileTmp);
        for (Iterator<Entry> i = mLruEntries.values().iterator(); i.hasNext(); ) {
            Entry entry = i.next();
            if (entry.mCurrentEditor == null) {
                for (int t = 0; t < mValueCount; t++) {
                    mSize += entry.mLengths[t];
                }
            } else {
                entry.mCurrentEditor = null;
                for (int t = 0; t < mValueCount; t++) {
                    deleteIfExists(entry.getCleanFile(t));
                    deleteIfExists(entry.getDirtyFile(t));
                }
                i.remove();
            }
        }
    }

    /**
     * Creates a new journal that omits redundant information. This replaces the
     * current journal if it exists.
     */
    private synchronized void rebuildJournal() throws IOException {
        if (mJournalWriter != null) {
            mJournalWriter.close();
        }

        Writer writer = new BufferedWriter(new FileWriter(mJournalFileTmp), IO_BUFFER_SIZE);
        writer.write(MAGIC);
        writer.write("\n");
        writer.write(VERSION_1);
        writer.write("\n");
        writer.write(Integer.toString(mAppVersion));
        writer.write("\n");
        writer.write(Integer.toString(mValueCount));
        writer.write("\n");
        writer.write("\n");

        for (Entry entry : mLruEntries.values()) {
            if (entry.mCurrentEditor != null) {
                writer.write(DIRTY + ' ' + entry.mKey + '\n');
            } else {
                writer.write(CLEAN + ' ' + entry.mKey + entry.getLengths() + '\n');
            }
        }

        writer.close();
        mJournalFileTmp.renameTo(mJournalFile);
        mJournalWriter = new BufferedWriter(new FileWriter(mJournalFile, true), IO_BUFFER_SIZE);
    }

    private static void deleteIfExists(File file) throws IOException {
//        try {
//            Libcore.os.remove(file.getPath());
//        } catch (ErrnoException errnoException) {
//            if (errnoException.errno != OsConstants.ENOENT) {
//                throw errnoException.rethrowAsIOException();
//            }
//        }
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    /**
     * @param key a
     * @return a snapshot of the entry named {@code key}, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     * @throws IOException a
     */
    public synchronized Snapshot get(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = mLruEntries.get(key);
        if (entry == null) {
            return null;
        }

        if (!entry.mReadable) {
            return null;
        }

        /*
         * Open all streams eagerly to guarantee that we see a single published
         * snapshot. If we opened streams lazily then the streams could come
         * from different edits.
         */
        InputStream[] ins = new InputStream[mValueCount];
        String[] filepath = new String[mValueCount];
        try {
            for (int i = 0; i < mValueCount; i++) {
                ins[i] = new FileInputStream(entry.getCleanFile(i));
                filepath[i] = entry.getCleanFile(i).getAbsolutePath();
            }
        } catch (FileNotFoundException e) {
            // a file must have been deleted manually!
            return null;
        }

        mRedundantOpCount++;
        mJournalWriter.append(READ + ' ' + key + '\n');
        if (journalRebuildRequired()) {
            mExecutorService.submit(mCleanupCallable);
        }

        return new Snapshot(key, entry.mSequenceNum, ins, filepath);
    }

    /**
     * @param key k
     * @return an editor for the entry named {@code key}, or null if another
     * edit is in progress.
     * @throws IOException a
     */
    public Editor edit(String key) throws IOException {
        return edit(key, ANY_SEQUENCE_NUMBER);
    }

    private synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = mLruEntries.get(key);
        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER
                && (entry == null || entry.mSequenceNum != expectedSequenceNumber)) {
            return null; // snapshot is stale
        }
        if (entry == null) {
            entry = new Entry(key);
            mLruEntries.put(key, entry);
        } else if (entry.mCurrentEditor != null) {
            return null; // another edit is in progress
        }

        Editor editor = new Editor(entry);
        entry.mCurrentEditor = editor;

        // flush the journal before creating files to prevent file leaks
        mJournalWriter.write(DIRTY + ' ' + key + '\n');
        mJournalWriter.flush();
        return editor;
    }

    /**
     * @return the directory where this cache stores its data.
     */
    public File getDirectory() {
        return mDirectory;
    }

    /**
     * @return the maximum number of bytes that this cache should use to store
     * its data.
     */
    public long maxSize() {
        return maxSize;
    }

    /**
     * @return the number of bytes currently being used to store the values in
     * this cache. This may be greater than the max size if a background
     * deletion is pending.
     */
    public synchronized long size() {
        return mSize;
    }

    private synchronized void completeEdit(Editor editor, boolean success) throws IOException {
        Entry entry = editor.mEntry;
        if (entry.mCurrentEditor != editor) {
            throw new IllegalStateException();
        }

        // if this edit is creating the entry for the first time, every index must have a value
        if (success && !entry.mReadable) {
            for (int i = 0; i < mValueCount; i++) {
                if (!entry.getDirtyFile(i).exists()) {
                    editor.abort();
                    throw new IllegalStateException("edit didn't create file " + i);
                }
            }
        }

        for (int i = 0; i < mValueCount; i++) {
            File dirty = entry.getDirtyFile(i);
            if (success) {
                if (dirty.exists()) {
                    File clean = entry.getCleanFile(i);
                    dirty.renameTo(clean);
                    long oldLength = entry.mLengths[i];
                    long newLength = clean.length();
                    entry.mLengths[i] = newLength;
                    mSize = mSize - oldLength + newLength;
                }
            } else {
                deleteIfExists(dirty);
            }
        }

        mRedundantOpCount++;
        entry.mCurrentEditor = null;
        if (entry.mReadable | success) {
            entry.mReadable = true;
            mJournalWriter.write(CLEAN + ' ' + entry.mKey + entry.getLengths() + '\n');
            if (success) {
                entry.mSequenceNum = mNextSequenceNumber++;
            }
        } else {
            mLruEntries.remove(entry.mKey);
            mJournalWriter.write(REMOVE + ' ' + entry.mKey + '\n');
        }

        if (mSize > maxSize || journalRebuildRequired()) {
            mExecutorService.submit(mCleanupCallable);
        }
    }

    /**
     * We only rebuild the journal when it will halve the size of the journal
     * and eliminate at least 2000 ops.
     */
    private boolean journalRebuildRequired() {
        final int REDUNDANT_OP_COMPACT_THRESHOLD = 2000;
        return mRedundantOpCount >= REDUNDANT_OP_COMPACT_THRESHOLD
                && mRedundantOpCount >= mLruEntries.size();
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @param key k
     * @return true if an entry was removed.
     * @throws IOException a
     */
    public synchronized boolean remove(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = mLruEntries.get(key);
        if (entry == null || entry.mCurrentEditor != null) {
            return false;
        }

        for (int i = 0; i < mValueCount; i++) {
            File file = entry.getCleanFile(i);
            if (!file.delete()) {
                throw new IOException("failed to delete " + file);
            }
            mSize -= entry.mLengths[i];
            entry.mLengths[i] = 0;
        }

        mRedundantOpCount++;
        mJournalWriter.append(REMOVE + ' ' + key + '\n');
        mLruEntries.remove(key);

        if (journalRebuildRequired()) {
            mExecutorService.submit(mCleanupCallable);
        }

        return true;
    }

    /**
     * @return true if this cache has been closed.
     */
    public boolean isClosed() {
        return mJournalWriter == null;
    }

    private void checkNotClosed() {
        if (mJournalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    /**
     * Force buffered operations to the filesystem.
     *
     * @throws IOException a
     */
    public synchronized void flush() throws IOException {
        checkNotClosed();
        trimToSize();
        mJournalWriter.flush();
    }

    /**
     * Closes this cache. Stored values will remain on the filesystem.
     *
     * @throws IOException a
     */
    public synchronized void close() throws IOException {
        if (mJournalWriter == null) {
            return; // already closed
        }
        for (Entry entry : new ArrayList<Entry>(mLruEntries.values())) {
            if (entry.mCurrentEditor != null) {
                entry.mCurrentEditor.abort();
            }
        }
        trimToSize();
        mJournalWriter.close();
        mJournalWriter = null;
    }

    private void trimToSize() throws IOException {
        while (mSize > maxSize) {
//            Map.Entry<String, Entry> toEvict = lruEntries.eldest();
            final Map.Entry<String, Entry> toEvict = mLruEntries.entrySet().iterator().next();
            remove(toEvict.getKey());
        }
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete
     * all files in the cache directory including files that weren't created by
     * the cache.
     *
     * @throws IOException a
     */
    public void delete() throws IOException {
        close();
        deleteContents(mDirectory);
    }

    private void validateKey(String key) {
        if (key.contains(" ") || key.contains("\n") || key.contains("\r")) {
            throw new IllegalArgumentException(
                    "keys must not contain spaces or newlines: \"" + key + "\"");
        }
    }

    private static String inputStreamToString(InputStream in) throws IOException {
        return readFully(new InputStreamReader(in, UTF_8));
    }

    /**
     * A snapshot of the values for an entry.
     */
    public final class Snapshot implements Closeable {
        private final String mKey;
        private final long mSequenceNumber;
        private final InputStream[] mIns;
        private final String[] mFilePath;

        private Snapshot(String key, long sequenceNumber, InputStream[] ins, String[] filePath) {
            this.mKey = key;
            this.mSequenceNumber = sequenceNumber;
            this.mIns = ins;
            this.mFilePath = filePath;
        }

        /**
         * @return an editor for this snapshot's entry, or null if either the
         * entry has changed since this snapshot was created or if another edit
         * is in progress.
         * @throws IOException a
         */
        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(mKey, mSequenceNumber);
        }

        /**
         * @param index i
         * @return the unbuffered stream with the value for {@code index}.
         */
        public InputStream getInputStream(int index) {
            return mIns[index];
        }

        /**
         * @param index i
         * @return the unbuffered stream with the value for {@code index}.
         */
        public String getFilePath(int index) {
            return mFilePath[index];
        }

        /**
         * @param index i
         * @return the string value for {@code index}.
         * @throws IOException a
         */
        public String getString(int index) throws IOException {
            return inputStreamToString(getInputStream(index));
        }

        @Override
        public void close() {
            for (InputStream in : mIns) {
                closeQuietly(in);
            }
        }
    }

    /**
     * Edits the values for an entry.
     */
    public final class Editor {
        private final Entry mEntry;
        private boolean mHasErrors;

        private Editor(Entry entry) {
            this.mEntry = entry;
        }

        /**
         * @param index i
         * @return an unbuffered input stream to read the last committed value,
         * or null if no value has been committed.
         * @throws IOException a
         */
        public InputStream newInputStream(int index) throws IOException {
            synchronized (DiskLruCache.this) {
                if (mEntry.mCurrentEditor != this) {
                    throw new IllegalStateException();
                }
                if (!mEntry.mReadable) {
                    return null;
                }
                return new FileInputStream(mEntry.getCleanFile(index));
            }
        }

        /**
         * @param index i
         * @return the last committed value as a string, or null if no value
         * has been committed.
         * @throws IOException a
         */
        public String getString(int index) throws IOException {
            InputStream in = newInputStream(index);
            return in != null ? inputStreamToString(in) : null;
        }

        /**
         * @param index i
         * @return a new unbuffered output stream to write the value at
         * {@code index}. If the underlying output stream encounters errors
         * when writing to the filesystem, this edit will be aborted when
         * {@link #commit} is called. The returned output stream does not throw
         * IOExceptions.
         * @throws IOException a
         */
        public OutputStream newOutputStream(int index) throws IOException {
            synchronized (DiskLruCache.this) {
                if (mEntry.mCurrentEditor != this) {
                    throw new IllegalStateException();
                }
                return new FaultHidingOutputStream(new FileOutputStream(mEntry.getDirtyFile(index)));
            }
        }

        /**
         * Sets the value at {@code index} to {@code value}.
         *
         * @param index i
         * @param value i
         * @throws IOException a
         */
        public void set(int index, String value) throws IOException {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(newOutputStream(index), UTF_8);
                writer.write(value);
            } finally {
                closeQuietly(writer);
            }
        }

        /**
         * Commits this edit so it is visible to readers.  This releases the
         * edit lock so another edit may be started on the same key.
         *
         * @throws IOException a
         */
        public void commit() throws IOException {
            if (mHasErrors) {
                completeEdit(this, false);
                remove(mEntry.mKey); // the previous entry is stale
            } else {
                completeEdit(this, true);
            }
        }

        /**
         * Aborts this edit. This releases the edit lock so another edit may be
         * started on the same key.
         *
         * @throws IOException a
         */
        public void abort() throws IOException {
            completeEdit(this, false);
        }

        private class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }

            @Override
            public void write(int oneByte) {
                try {
                    out.write(oneByte);
                } catch (IOException e) {
                    mHasErrors = true;
                }
            }

            @Override
            public void write(byte[] buffer, int offset, int length) {
                try {
                    out.write(buffer, offset, length);
                } catch (IOException e) {
                    mHasErrors = true;
                }
            }

            @Override
            public void close() {
                try {
                    out.close();
                } catch (IOException e) {
                    mHasErrors = true;
                }
            }

            @Override
            public void flush() {
                try {
                    out.flush();
                } catch (IOException e) {
                    mHasErrors = true;
                }
            }
        }
    }

    private final class Entry {
        private final String mKey;

        /**
         * Lengths of this entry's files.
         */
        private final long[] mLengths;

        /**
         * True if this entry has ever been published.
         */
        private boolean mReadable;

        /**
         * The ongoing edit or null if this entry is not being edited.
         */
        private Editor mCurrentEditor;

        /**
         * The sequence number of the most recently committed edit to this entry.
         */
        private long mSequenceNum;

        private Entry(String key) {
            this.mKey = key;
            this.mLengths = new long[mValueCount];
        }

        public String getLengths() throws IOException {
            StringBuilder result = new StringBuilder();
            for (long size : mLengths) {
                result.append(' ').append(size);
            }
            return result.toString();
        }

        /**
         * Set lengths using decimal numbers like "10123".
         */
        private void setLengths(String[] strings) throws IOException {
            if (strings.length != mValueCount) {
                throw invalidLengths(strings);
            }

            try {
                for (int i = 0; i < strings.length; i++) {
                    mLengths[i] = Long.parseLong(strings[i]);
                }
            } catch (NumberFormatException e) {
                throw invalidLengths(strings);
            }
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        public File getCleanFile(int i) {
            return new File(mDirectory, mKey + "." + i);
        }

        public File getDirtyFile(int i) {
            return new File(mDirectory, mKey + "." + i + ".tmp");
        }
    }
}
