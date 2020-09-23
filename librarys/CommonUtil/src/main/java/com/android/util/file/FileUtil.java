package com.android.util.file;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 *
 * @author 张全
 */
public final class FileUtil {

    private FileUtil() {
    }


    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * 将bitmap保存为jpg图片
     *
     * @param bitmap
     * @param context
     * @param phone
     * @param fileName
     * @return
     */
    public static String outBitmapToFile(Bitmap bitmap, Context context, String fileName) {
        File imageFile = createFile(context, fileName);
        if (imageFile == null) {
            return null;
        }

        outBitmapToFile(bitmap, imageFile);
        return imageFile.getAbsolutePath();
    }

    /**
     * 创建图片文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File createFile(Context context, String fileName) {
        try {

            if (TextUtils.isEmpty(fileName)) {
                fileName = System.currentTimeMillis() + ".jpg";
            }

            File targetFile = new File(context.getCacheDir(), fileName);

            if (!targetFile.createNewFile()) {
                return null;
            } else {
                return targetFile;
            }

            // if (!TextUtils.isEmpty(fileName)) {
            // File imageFile = new File(fileName);
            // if (imageFile.exists() && imageFile.canRead()) {
            // imageFile.delete();
            // }
            // }
            //
            // File cache = new File(context.getExternalCacheDir(), CACHE_DIR);
            // if (!cache.exists() && !cache.canRead()) {
            // cache.mkdirs();
            // }
            //
            // StringBuffer pathBuff = new
            // StringBuffer(cache.getCanonicalPath());
            // pathBuff.append(File.separatorChar).append(String.valueOf(System.currentTimeMillis())
            // + ".jpg");
            // File imageFile = new File(pathBuff.toString());
            // imageFile.createNewFile();
            // return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建一个目录
     *
     * @return
     */
    public static boolean createDir(File dirPath) {
        if (dirPath == null) {
            return false;
        }
        if (!dirPath.exists()) {
            return dirPath.mkdirs();
        }
        return false;

    }

    /**
     * 创建一个.nomedia文件
     *
     * @param targetDir
     * @return
     * @throws IOException
     */
    public static boolean createNoMediaFile(File targetDir) throws IOException {
        if (targetDir == null) {
            return false;
        } else {
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }
        return new File(targetDir, ".nomedia").createNewFile();
    }

    /**
     * 删除目录及其子文件.
     *
     * @param file 文件夹
     */
    public static void delFile(File file) {
        if (null == file)
            return;
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (null == listFiles || listFiles.length == 0) {
                file.delete();
            } else {
                for (File item : listFiles) {
                    delFile(item);
                    item.delete();
                }
            }
        } else
            file.delete();
        file.delete();
    }

    /**
     * 获得文件目录大小
     *
     * @param dir 文件
     * @return 文件目录大小
     */
    public static long getFileSize(File file) {
        long length = 0;
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                File dir = new File(file, children[i]);
                length += getFileSize(dir);
            }
        } else
            length = file.length();
        return length;
    }

    /**
     * 获取文件的扩展名.
     *
     * @param fileName 文件名 比如 java.txt
     * @return 该文件的扩展名 比如txt
     */
    @SuppressLint("DefaultLocale")
    public static String getExtension(String fileName) {
        String mExt = "";
        if (!TextUtils.isEmpty(fileName)) {
            final int index = fileName.lastIndexOf('.');
            mExt = (index != -1) ? fileName.substring(index + 1).toLowerCase().intern() : "";
        }
        return mExt;
    }

    /**
     * 获取文件名(不带扩展名).
     *
     * @param fileName 文件名 比如java.txt
     * @return 文件名 比如java
     */
    public static String getFileName(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            int index = fileName.lastIndexOf('.');
            return index == -1 ? null : fileName.substring(0, index);
        }
        return null;
    }

    /**
     * 缩短显示指定的文件路径
     *
     * @param path 当前目录
     * @param dim  层级
     * @return 返回最后dim层目录 比如/a/b/c/d.txt dim=2 则返回 /c/d.txt
     */
    public static String getFileShortPath(String path, int dim) {
        int index = path.lastIndexOf("/");
        String shortPath = null;
        if (index == -1) {
            shortPath = "/" + path;
        } else if (index > 0) {
            for (int i = 1; i < dim; i++) {
                int subIndex = path.lastIndexOf("/", index - 1);
                if (subIndex == -1) {
                    break;
                } else {
                    index = subIndex;
                }
            }
            shortPath = path.substring(index);
        }
        return shortPath;
    }

    /**
     * 获取当前目录的父级目录.
     *
     * @param mCurrentPath 当前目录
     * @return 上级目录 比如当前目录为/a/b/c.txt 则返回/a/b
     */
    public static String getParentFilePath(String mCurrentPath) {
        int indexLastSlash = mCurrentPath.lastIndexOf("/");
        return -1 == indexLastSlash ? null : mCurrentPath.substring(0, indexLastSlash);
    }

    /**
     * 拷贝文件。
     *
     * @param context
     * @param srcFile  源文件
     * @param fileName 目标文件名
     * @return boolean true拷贝成功
     */
    public static String copyFile(Context context, File srcFile, String fileName) {
        if (null == srcFile || !srcFile.exists()) {
            return null;
        }

        FileChannel fcIn = null;
        FileChannel fcOut = null;

        File targetFile = createFile(context, fileName);
        if (targetFile == null || !targetFile.exists()) {
            return null;
        }

        try {
            fcIn = new FileInputStream(srcFile).getChannel();
            fcOut = new FileOutputStream(targetFile).getChannel();
            fcIn.transferTo(0, fcIn.size(), fcOut);
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fcIn) {
                try {
                    fcIn.close();
                } catch (Exception e) {
                }
            }
            if (null != fcOut) {
                try {
                    fcOut.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    /**
     * 拷贝文件。
     *
     * @param srcFile    源文件
     * @param targetFile 目标文件
     * @return boolean true拷贝成功
     */
    public static boolean copyFile(File srcFile, File targetFile) {
        if (null == srcFile || !srcFile.exists()) {
            return false;
        }

        FileChannel fcIn = null;
        FileChannel fcOut = null;
        try {
            if (null == targetFile || !targetFile.exists()) {
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
            }
            fcIn = new FileInputStream(srcFile).getChannel();
            fcOut = new FileOutputStream(targetFile).getChannel();
            fcIn.transferTo(0, fcIn.size(), fcOut);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fcIn) {
                try {
                    fcIn.close();
                } catch (Exception e) {
                }
            }
            if (null != fcOut) {
                try {
                    fcOut.close();
                } catch (Exception e) {
                }
            }
        }
        return false;
    }

    /**
     * 检查目录是否存在，并且为目录文件。
     *
     * @param dir 要检查的目录文件
     * @return boolean
     */
    public static boolean isDirExist(File dir) {
        return isFileExist(dir) && dir.isDirectory();
    }

    /**
     * 检查文件是否存在，已做非空检查，file为null时认为文件不存在。
     *
     * @param file 要检查的文件
     * @return boolean
     */
    public static boolean isFileExist(File file) {
        if (null == file) {
            return false;
        }
        return file.exists();
    }

    /**
     * 写文件,如果文件存在 则覆盖
     *
     * @param from 数据流
     * @param to   目标文件
     */
    public static void outStreamToFile(InputStream from, File to) {
        FileOutputStream os = null;
        try {
            if (to.exists())
                to.delete();
            to.getParentFile().mkdirs();
            to.createNewFile();
            os = new FileOutputStream(to);
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = from.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                }
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                }
        }
    }

    /**
     * 将bitmap保存为jpg图片
     *
     * @param bitmap
     * @param file
     */
    public static void outBitmapToFile(Bitmap bitmap, File file) {
        if (null == bitmap || null == file)
            return;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (null != file)
                file.delete();
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File saveResToFile(Context context, int imgId, File imgFile) {
        try {
            Drawable iconDrawable = context.getResources().getDrawable(imgId);
            if (iconDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
                if (bitmap != null) {

                    // File imgFile = createImageFile(context, SHARE_IMG);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(CompressFormat.JPEG, 100, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    // write the bytes in file
                    FileOutputStream fos = new FileOutputStream(imgFile);
                    fos.write(bitmapdata);
                    fos.close();
                    return imgFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveImage(ImageView appIcon, Context context,
                                 File imgFile) {
        try {
            Drawable iconDrawable = appIcon.getDrawable();
            if (iconDrawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) iconDrawable).getBitmap();
                if (bitmap != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(CompressFormat.JPEG, 100, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    FileOutputStream fos = new FileOutputStream(imgFile);
                    fos.write(bitmapdata);
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImage(Bitmap bitmap, Context context, File imgFile) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(imgFile);
            fos.write(bitmapdata);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveImage(Bitmap bitmap, CompressFormat format,
                                 Context context, File imgFile) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(format, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(imgFile);
            fos.write(bitmapdata);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
