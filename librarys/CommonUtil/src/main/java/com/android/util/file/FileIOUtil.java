package com.android.util.file;

import android.text.TextUtils;
import android.util.Log;

import com.android.util.log.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * 文件IO操作类，包含读写方面的方法.
 *
 * @author 张全
 */

public final class FileIOUtil {
    private static final String TAG = FileIOUtil.class.getSimpleName();
    private static final int EOF = -1;

    private FileIOUtil() {
    }

    /**
     * 读取文件指定位置的内容到buffer中。
     *
     * @param buffer   读取到的字节
     * @param file     要读取的文件
     * @param startPos 文件的起始位置
     * @param length   读取的长度
     * @return int 读取的长度
     * @throws FileNotFoundException 文件未找到
     */
    public static int read(byte[] buffer, File file, int startPos, int length)
            throws FileNotFoundException {
        if (!isParamsValidate(buffer, file)) {
            return 0;
        }

        if (!file.exists()) {
            return 0;
        }

        if (isOutOfBounds(startPos, startPos + length, file.length())) {
            return 0;
        }

        RandomAccessFile is = null;
        try {
            is = new RandomAccessFile(file, "r");

            is.seek(startPos);
            return is.read(buffer, 0,
                    (int) (Math.min(file.length() - startPos, length)));
        } catch (IOException e) {
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
            } catch (Exception e2) {
            }
        }

        return EOF;
    }

    /**
     * 将data写入指定文件的指定位置，如果文件不存在，自行创建，如果文件已存在，覆盖原文件，不能用于循环写入文件！。
     *
     * @param data     要写入的数据
     * @param file     指定的文件
     * @param startPos 指定文件的起始位置
     * @return boolean
     */
    public static boolean write(byte[] data, File file, int startPos) {
        return write(data, file, startPos, true);
    }

    /**
     * 将data写入指定文件的指定位置。
     *
     * @param data       要写入的数据
     * @param file       指定的文件
     * @param startPos   指定文件的起始位置
     * @param isOverride 是否覆盖原文件
     * @return boolean
     */
    public static boolean write(byte[] data, File file, int startPos,
                                boolean isOverride) {
        if (!isParamsValidate(data, file)) {
            return false;
        }

        // 写入文件startPos可以从file.length开始
        if (startPos < 0 || startPos > file.length()) {
            return false;
        }

        RandomAccessFile os = null;

        try {
            // 如果文件上级目录不存在，先创建目录
            if (!FileUtil.isFileExist(file)) {
                file.getParentFile().mkdirs();
            }

            // 如果需要覆盖文件，且文件已存在，删除原文件
            if (isOverride && FileUtil.isFileExist(file)) {
                FileUtil.delFile(file);
            }

            os = new RandomAccessFile(file, "rw");
            os.seek(startPos);
            os.write(data, 0, data.length);
            return true;
        } catch (Exception e) {
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (IOException e2) {
                LogUtil.d(TAG, "write, close bis IOException");
            }
        }
        return false;
    }

    /**
     * 读取全部文件内容到buffer中，大与500K的文件慎用，应分段读取
     *
     * @param buffer 读取到的字节
     * @param file   要读取的文件
     * @return int 读取的长度
     * @throws FileNotFoundException 文件未找到
     */
    public static int read(byte[] buffer, File file)
            throws FileNotFoundException {
        if (!isParamsValidate(buffer, file)) {
            Log.w(TAG, "read, buffer of file is null, return 0");
            return 0;
        }

        if (!file.exists()) {
            Log.w(TAG, "read, file not exist, return 0");
            return 0;
        }

        BufferedInputStream bis = null;

        try {
            bis = getBufferedInputStream(file);

            return bis.read(buffer, 0, buffer.length);

        } catch (FileNotFoundException e) {
            Log.w(TAG, "read, fileNotFound, file:" + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (null != bis) {
                    bis.close();
                }
            } catch (IOException e2) {
                LogUtil.d(TAG, "read, close bis IOException");
            }
        }

        return EOF;
    }

    /**
     * 将数据写入文件，会覆盖已有文件，不能用于循环写入文件！
     * {@link FileIOUtil#write(byte[], File, boolean)}。
     *
     * @param data 写入的数据
     * @param file 写入的文件
     * @return boolean
     */
    public static boolean write(byte[] data, File file) {
        return write(data, file, true);
    }

    /**
     * 将数据写入文件。
     *
     * @param data       要写入的数据
     * @param file       写入的文件
     * @param isOverride 是否覆盖原文件
     * @return boolean
     */
    public static boolean write(byte[] data, File file, boolean isOverride) {
        if (!isParamsValidate(data, file)) {
            Log.w(TAG, "write, buffer of file is null, return false");
            return false;
        }

        BufferedOutputStream bos = null;

        try {
            // 如果文件上级目录不存在，先创建目录
            if (!FileUtil.isFileExist(file)) {
                file.getParentFile().mkdirs();
            }

            // 如果需要覆盖文件，且文件已存在，删除原文件
            if (isOverride && FileUtil.isFileExist(file)) {
                FileUtil.delFile(file);
            }

            bos = getBufferedOutputStream(file);
            bos.write(data, 0, data.length);
            bos.flush();
            return true;
        } catch (FileNotFoundException e) {
            Log.w(TAG,
                    "write, fileNotFound, file:" + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException e2) {
                LogUtil.d(TAG, "write, close bis IOException");
            }
        }
        return false;
    }

    /**
     * 从InputStream中读取指定长度,需要客户端自行关闭流文件。
     *
     * @param is          要读入的流文件
     * @param buffer      要返回的buffer
     * @param bufferStart buffer数组的起始位置，一般为0
     * @param length      希望读取的长度
     * @return int 实际读取的长度
     * @throws IOException IO异常
     */
    public static int read(InputStream is, byte[] buffer, int bufferStart,
                           int length) throws IOException {
        if (null == is || null == buffer) {
            Log.w(TAG, "read, fileInputStream is null, return 0");
            return 0;
        }

        if (isOutOfBounds(bufferStart, bufferStart + length, buffer.length)) {
            Log.w(TAG,
                    "read, start of length out of bounds, return 0. start:"
                            + bufferStart + " length:" + length
                            + " bufferLength:" + buffer.length);
            return 0;
        }

        return is.read(buffer, bufferStart, length);
    }

    /**
     * 将输入流写入指定的文件，大与500K的流文件请分块写入，需要客户端自行关闭流文件，默认覆盖已有文件.
     *
     * @param is   输入的流文件
     * @param file 要写入的文件
     * @return boolean 是否成功写入
     * @throws IOException IO异常
     */
    public static boolean write(InputStream is, File file) throws IOException {
        return write(is, file, is.available());
    }

    /**
     * 将输入流写入指定文件，大与500K的流文件请分块写入，需要客户端自行关闭流文件，默认覆盖已有文件.
     *
     * @param is     输入的流文件
     * @param file   要写人的文件
     * @param length 写入长度
     * @return boolean 是否成功写入
     * @throws IOException IO异常
     */
    public static boolean write(InputStream is, File file, int length)
            throws IOException {
        return write(is, file, length, true);
    }

    /**
     * 将输入流写入指定的文件.需要客户端自行关闭流文件 。
     *
     * @param is         要读入的流文件
     * @param file       要写入的文件
     * @param length     要写入的长度
     * @param isOverride 是否覆盖已有文件
     * @return boolean 是否成功写入
     * @throws IOException IO读取输入流时异常
     */
    public static boolean write(InputStream is, File file, int length,
                                boolean isOverride) throws IOException {
        if (null == is || null == file) {
            Log.w(TAG,
                    "write, fileInputStream or file is null, return false");
            return false;
        }

        if (isOutOfBounds(0, length, is.available())) {
            Log.w(
                    TAG,
                    "write, length is out of bounds, return false. length:"
                            + length + " inputstream.available:"
                            + is.available());
            return false;
        }

        // 如果文件上级目录不存在，先创建目录
        if (!FileUtil.isFileExist(file)) {
            file.getParentFile().mkdirs();
        }

        // 如果需要覆盖文件，且文件已存在，删除原文件
        if (isOverride && FileUtil.isFileExist(file)) {
            FileUtil.delFile(file);
        }

        byte[] buffer = new byte[length];
        is.read(buffer, 0, length);

        BufferedOutputStream os = null;
        try {
            os = getBufferedOutputStream(file);
            os.write(buffer, 0, length);
            os.flush();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (Exception e2) {
                LogUtil.d(TAG, "write close os error");
            }
        }

        return true;
    }

    /**
     * 得到文件的inputStream，需要客户端对文件流进行close. 推荐使用getBufferedInputStream
     * {@link FileIOUtil#getBufferedInputStream}
     *
     * @param filePath 目标文件的绝对路径
     * @return fileInputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static FileInputStream getFileInputStream(String filePath)
            throws FileNotFoundException {
        if (!isFilePathValidate(filePath)) {
            Log.w(TAG,
                    "getInputStream, filePath is invalidate, return null");
            return null;
        }

        return getFileInputStream(new File(filePath));
    }

    /**
     * 得到文件的inputStream，需要客户端对文件流进行close. 推荐使用getBufferedInputStream
     * {@link FileIOUtil#getBufferedInputStream}
     *
     * @param file 目标文件
     * @return fileInputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static FileInputStream getFileInputStream(File file)
            throws FileNotFoundException {
        return new FileInputStream(file);
    }

    /**
     * 得到文件的outputStream，需要客户端对文件流进行close. 推荐使用getBufferedOutputStream
     * {@link FileIOUtil#getBufferedOutputStream}
     *
     * @param filePath 目标文件的绝对路径
     * @return fileInputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static FileOutputStream getFileOutputStream(String filePath)
            throws FileNotFoundException {
        if (!isFilePathValidate(filePath)) {
            Log.w(TAG,
                    "getOutputStream, filePath is invalidate, return null");
            return null;
        }

        return getFileOutputStream(new File(filePath));
    }

    /**
     * 得到文件的outputStream，需要客户端对文件流进行close. 推荐使用getBufferedOutputStream
     * {@link FileIOUtil#getBufferedOutputStream}
     *
     * @param file 目标文件
     * @return fileInputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static FileOutputStream getFileOutputStream(File file)
            throws FileNotFoundException {
        return new FileOutputStream(file, true);
    }

    /**
     * 读取文件的bufferInputStream 需要客户端调用结束后手动关闭流文件。
     *
     * @param filePath 文件绝对路径
     * @return BufferedInputStream or null
     * @throws FileNotFoundException 文件未找到
     */
    public static BufferedInputStream getBufferedInputStream(String filePath)
            throws FileNotFoundException {
        if (!isFilePathValidate(filePath)) {
            Log.w(TAG, "getBufferedInputStream, filePath is invalidate");
            return null;
        }

        return getBufferedInputStream(new File(filePath));
    }

    /**
     * 读取文件的bufferInputStream 需要客户端调用结束后手动关闭流文件。
     *
     * @param file 文件
     * @return BufferedInputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static BufferedInputStream getBufferedInputStream(File file)
            throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    /**
     * 获取写文件的BufferedOutputStream 需要客户端调用结束后手动关闭流文件。
     *
     * @param filePath 文件绝对路径
     * @return BufferedOutputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static BufferedOutputStream getBufferedOutputStream(String filePath)
            throws FileNotFoundException {
        if (!isFilePathValidate(filePath)) {
            Log.w(TAG,
                    "getBufferedOutputStream, filePath is invalidate, return null");
            return null;
        }

        return getBufferedOutputStream(new File(filePath));
    }

    /**
     * 获取写文件的gBufferedOutputStream 需要客户端调用结束后手动关闭流文件。
     *
     * @param file 文件
     * @return BufferedOutputStream
     * @throws FileNotFoundException 文件未找到
     */
    public static BufferedOutputStream getBufferedOutputStream(File file)
            throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(file, true));
    }

    private static boolean isFilePathValidate(String path) {
        return TextUtils.isEmpty(path);
    }

    private static boolean isParamsValidate(byte[] data, File file) {
        return null != data && null != file;
    }

    private static boolean isOutOfBounds(int start, int end, long length) {
        if ((start | end - start | length - end) < 0) {
            return true;
        }

        return false;
    }
}
