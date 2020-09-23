package com.android.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.util.LContext;
import com.android.util.os.DeviceUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * 图片处理工具类
 */
public class ImageUtil {
    private ImageUtil() {
    }

    public  static void setImageMatchScreenWidth(ImageView imageView,int defaultWidth){
        BitmapDrawable  bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if(bitmapDrawable == null)
            return;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if(bitmap == null)
            return;
        float scale = (float) defaultWidth / (float) bitmap.getWidth();
        int defaultHeight = Math.round(bitmap.getHeight() * scale);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = defaultWidth;
        params.height = defaultHeight;
        imageView.setLayoutParams(params);
    }

    /**
     * 质量压缩方法
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;
        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    /**
     * 处理图片圆角
     *
     * @param bitmap
     * @param radius 圆角幅度/半径
     * @return
     */
    public static Bitmap getCornerBitmap(Bitmap bitmap, float radius) {
        if (bitmap == null)
            return null;
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(15);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    /**
     * @param ctx
     * @param imgRes
     * @param radius dp单位
     * @return
     */
    private static Drawable getRadiusDrawable(Context ctx, int imgRes, int radius) {
        radius = DeviceUtil.dip2px(ctx, radius);
        Bitmap toTransform = BitmapFactory.decodeResource(ctx.getResources(), imgRes);
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(toTransform, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        return new BitmapDrawable(ctx.getResources(), bitmap);
    }

    /**
     * 获得带倒影的图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getReflectionImageWithOrigin(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        // 从高度的2/3-->2/3+1/3（即全部）开始取倒影
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height * 2 / 3,
                width, height * 1 / 3, matrix, false);
        reflectionImage = setAlpha(reflectionImage, 50);// 一半透明
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + height / 3), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint defaultPaint = new Paint();
        canvas.drawRect(0 + 1, height + 1, width - 1, height + 1, defaultPaint);
        canvas.drawBitmap(reflectionImage, 0, height, null);
        return bitmapWithReflection;
    }

    /**
     * 设置透明度渐变
     *
     * @param sourceImg
     * @param number
     * @return
     */
    private static Bitmap setAlpha(Bitmap sourceImg, int number) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,
                sourceImg.getWidth(), sourceImg.getHeight());
        number = number * 255 / 100;
        double round = (double) number / (double) (argb.length);
        for (int i = 0; i < argb.length; i++) {
            if (number - i * round > 10) {
                argb[i] = ((int) (number - i * round) << 24)
                        | (argb[i] & 0x00FFFFFF);
                continue;
            } else {
                argb[i] = (10 << 24) | (argb[i] & 0x00FFFFFF);
                continue;
            }
        }
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(),
                sourceImg.getHeight(), Config.ARGB_8888);
        return sourceImg;
    }

    /**
     * 获取圆形图片
     *
     * @param src
     * @param radius
     * @return
     */
    public static Bitmap getCircleBitmap(Bitmap src, int radius) {
        Bitmap dstBitmap = Bitmap.createBitmap(radius * 2, radius * 2,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        Path path = new Path();
        path.addCircle(radius, radius, radius, Direction.CCW);
        canvas.clipPath(path);

        // CenterCrop
        int bw = src.getWidth() / 2;
        int bh = src.getHeight() / 2;
        int left = radius >= bw ? 0 : bw - radius;
        int top = radius >= bh ? 0 : bh - radius;
        int right = left == 0 ? bw : left + radius;
        int bottom = top == 0 ? bh : top + radius;
        Rect srcRect = new Rect(left, top, right, bottom);
        Rect dstRect = new Rect(0, 0, 2 * radius, 2 * radius);
        canvas.drawBitmap(src, srcRect, dstRect, paint);
        return dstBitmap;
    }

    /**
     * 将Drawable装换为Bitmap.
     *
     * @param drawable 原始drawable
     * @return 装换后的bitmap
     */
    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 按目标宽高进行缩放
     *
     * @param bitmap
     * @param dstW
     * @param dstH
     * @return
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int dstW, int dstH) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = (float) dstW / w;
        float scaleHeight = (float) dstH / h;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    /**
     * 获取图标 res
     *
     * @param icon
     * @return
     */
    public static int getIconRes(String icon) {
        Context ctx = LContext.getContext();
        return ctx.getResources().getIdentifier(icon, "drawable",
                ctx.getPackageName());
    }

    public static Bitmap scaleBySW(Context ctx, Bitmap src) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int sw = dm.widthPixels;
        int bw = src.getWidth();
        if (sw == bw) {
            return src;
        }

        int bh = src.getHeight();
        int targetH = Math.round(bh * sw * 1.0f / bw);
        return Bitmap.createScaledBitmap(src, sw, targetH, true);
    }

    public static byte[] readUri(Context context, Uri uri, CompressFormat format) {

        try {
            Bitmap bitmap = ImageIOUtil.getInstance(context).resizeBitmap(uri);
            if (bitmap == null) {
                return new byte[0];
            }
            return compressImage(bitmap, MAX_SIZE, format);// 压缩好比例大小后再进行质量压缩
            // return
            // comp(bitmap,
            // MAX_SIZE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static byte[] compressImage(Bitmap image, int maxKb,
                                       CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(format, 100, baos);
        int options = 100;
        byte[] bytes = baos.toByteArray();
        while (bytes.length / 1024 > maxKb) {
            baos.reset();
            options -= 10;
            image.compress(format, options, baos);
            bytes = baos.toByteArray();
            if (options <= 10) {
                break;
            }
        }
        if (!image.isRecycled()) {
            image.recycle();
            System.gc();
            image = null;
        }
        return bytes;
    }

    public static void reclyBitmap(Bitmap bitmap) {
        try {
            if (null != bitmap && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (Exception e) {

        } finally {
            bitmap = null;
        }
    }

    private static final int MAX_SIZE = 100;
}
