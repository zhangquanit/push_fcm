package com.android.util.ext;

import android.app.Activity;
import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SoftInputUtil {
    public static void showSoftInput(final EditText inputET, Context context) {
        inputET.requestFocus();
        final InputMethodManager inputManger = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManger.showSoftInput(inputET, InputMethodManager.RESULT_SHOWN);
        inputManger.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 隐藏键盘
     *
     * @param inputET
     */
    public static void hideSoftInput(final EditText inputET, Context context) {
        inputET.clearFocus();
        InputMethodManager inputManger = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManger.hideSoftInputFromWindow(inputET.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public static void hideSoftInput(Activity context) {
        View currentFocus = context.getCurrentFocus();
        if (currentFocus == null) {
            return;
        }
        currentFocus.clearFocus();
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public static void setLastSelection(EditText editText) {
        CharSequence text = editText.getText();
        if (text instanceof Spannable) {
            Spannable spanText = (Spannable) text;
            Selection.setSelection(spanText, text.length());
        }
    }
}
