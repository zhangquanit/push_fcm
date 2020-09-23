package com.sndo.fcm.demo4.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.sndo.fcm.demo4.R;

import java.lang.reflect.Field;

public class ToastUtil {
    private static Toast mToast;
    private static Field sField_TN;
    private static Field sField_TN_Handler;

    static {
        try {
            sField_TN = Toast.class.getDeclaredField("mTN");
            sField_TN.setAccessible(true);
            sField_TN_Handler = sField_TN.getType().getDeclaredField("mHandler");
            sField_TN_Handler.setAccessible(true);
        } catch (Exception e) {
        }
    }

    private static void hook(Toast toast) {
        try {
            Object tn = sField_TN.get(toast);
            Handler preHandler = (Handler) sField_TN_Handler.get(tn);
            sField_TN_Handler.set(tn, new SafelyHandlerWarpper(preHandler));
        } catch (Exception e) {
        }
    }

    private static class SafelyHandlerWarpper extends Handler {

        private Handler impl;

        public SafelyHandlerWarpper(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                super.dispatchMessage(msg);
            } catch (Exception e) {
            }
        }

        @Override
        public void handleMessage(Message msg) {
            impl.handleMessage(msg);//需要委托给原Handler执行
        }
    }

    public static void show(int resId) {
        show(LContext.getString(resId));
    }

    public static void show(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        if (mToast == null) {
            Context context = LContext.getContext();
//			mToast = Toast.makeText(LContext.getContext(), text,
//					Toast.LENGTH_SHORT);
            int d14 = dip2px(context, 14);
            int d8 = dip2px(context, 8);
            mToast = new Toast(context);
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextSize(15);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.toast_bg);
            textView.setPadding(d14, d8, d14, d8);
            mToast.setView(textView);
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        hook(mToast);
        mToast.show();
    }

    public static void showLong(int resId) {
        showLong(LContext.getString(resId));
    }

    public static void showLong(CharSequence text) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }

        if (mToast == null) {
            Context context = LContext.getContext();
//            mToast = Toast.makeText(context, text,
//					Toast.LENGTH_LONG);
            int d14 = dip2px(context, 14);
            int d8 = dip2px(context, 8);
            mToast = new Toast(context);
            TextView textView = new TextView(context);
            textView.setText(text);
            textView.setTextSize(15);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setBackgroundResource(R.drawable.toast_bg);
            textView.setPadding(d14, d8, d14, d8);
            mToast.setView(textView);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        hook(mToast);
        mToast.show();
    }

    public static void cancel() {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
    }

    public static int dip2px(Context context, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
                context.getResources().getDisplayMetrics());
    }
}
