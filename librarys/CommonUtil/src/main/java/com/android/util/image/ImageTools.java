package com.android.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImageTools {
    public static final int REQUEST_WIDTH = 600;
    public static final int REQUEST_HEIGHT = 800;

    /**
     * 获取本地图片
     *
     * @param ctx
     * @param uri
     * @return
     */
    public static Bitmap getImageLocal(Context ctx, Uri uri) {
        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        Bitmap srcBitmap = null;
        if (metrics.density <= 1.5f) {
            srcBitmap = decodeBitmap(ctx, uri);
        } else {
            srcBitmap = getImageLocal(ctx, uri, metrics.widthPixels, metrics.heightPixels);
        }
        return srcBitmap;
    }

    /**
     * 选择照片
     *
     * @param ctx
     * @param srcUri
     * @return
     */
    public static Bitmap getRotateImageLocal(Context ctx, Uri srcUri) {
        Bitmap srcBitmap = getImageLocal(ctx, srcUri);
        return rotateBitmap(srcBitmap, srcUri.getPath());
    }

    /**
     * 选择角度
     *
     * @param bitmap
     * @param filePath
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, String filePath) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        final int orientation = readPictureDegree(filePath);
        if (orientation == 0) {
            return bitmap;
        }

        Matrix m = new Matrix();
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        m.setRotate(orientation);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return newBitmap;
    }

    /**
     * 获取本地图片
     *
     * @param ctx
     * @param uri
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap getImageLocal(Context ctx, Uri uri, int reqWidth, int reqHeight) {
        if (reqWidth == -1 || reqHeight == -1) {
            return decodeBitmap(ctx, uri);
        } else {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            decodeBitmap(ctx, options, uri);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return decodeBitmap(ctx, options, uri);
        }
    }

    public static Bitmap decodeBitmap(Context ctx, Options options, Uri srcUri) {
        try {
            Bitmap srcBitmap = BitmapFactory.decodeStream(ctx
                            .getContentResolver().openInputStream(srcUri), null,
                    options);

            return srcBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap decodeBitmap(Context ctx, Uri srcUri) {
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        bmpFactoryOptions.inSampleSize = 2;
        bmpFactoryOptions.inJustDecodeBounds = false;

        try {
            Bitmap srcBitmap = BitmapFactory.decodeStream(ctx
                            .getContentResolver().openInputStream(srcUri), null,
                    bmpFactoryOptions);

            return srcBitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取角度
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
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
     * Save image to the SD card
     *
     * @param photoBitmap
     * @param photoName
     * @param path
     */
    public static void savePhotoToSDCard(Bitmap photoBitmap, String path,
                                         String photoName) {
        if (checkSDCardAvailable()) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                            fileOutputStream)) {
                        fileOutputStream.flush();
                        // fileOutputStream.close();
                    }
                }
                // if (photoBitmap != null && !photoBitmap.isRecycled()) {
                // photoBitmap.recycle();
                // System.gc();
                // }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Check the SD card
     *
     * @return
     */
    public static boolean checkSDCardAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }


    public static int calculateInSampleSize(Options options) {
        return calculateInSampleSize(options, REQUEST_WIDTH, REQUEST_HEIGHT);
    }

    public static int calculateInSampleSize(Options options, int reqWidth,
                                            int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to
            // the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
