
package com.android.util.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;

public final class ImageIOUtil {
    private Context context = null;

    private static ImageIOUtil ioUtil;

    public static ImageIOUtil getInstance(Context context) {
        if (ioUtil == null) {
            synchronized (ImageIOUtil.class) {
                ioUtil = new ImageIOUtil(context);
            }
        }
        return ioUtil;
    }

    private ImageIOUtil(Context context) {
        this.context = context;
    }

    /**
     * 根据路径获得图片并压缩，返回bitmap用于显示
     *
     * @param uri
     * @return
     * @throws FileNotFoundException
     */
    public Bitmap resizeBitmap(Uri uri) throws FileNotFoundException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory
                .decodeStream(context.getContentResolver().openInputStream(uri), null, options);

        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, options);
        int degree = readPictureDegree(uri.getPath());
        if (degree != 0) {// 旋转照片角度
            bitmap = rotateBitmap(bitmap, degree);
        }
        return bitmap;
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 读取图片角度
     *
     * @param path
     * @return
     */
    @SuppressLint("NewApi")
    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片角度
     *
     * @param bitmap
     * @param degress
     * @return
     */
    public Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        Bitmap newBitMap = null;
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                System.gc();
                bitmap = null;
            }
            newBitMap = createBitmap;
        }
        return newBitMap;
    }
}
