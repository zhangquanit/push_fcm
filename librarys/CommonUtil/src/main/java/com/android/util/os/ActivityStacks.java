package com.android.util.os;

import android.app.Activity;

import java.util.Stack;

/**
 * Activity栈
 */
public class ActivityStacks {

    private Stack<Activity> mActivityStack;
    private static ActivityStacks mStacks;

    ActivityStacks() {
        mActivityStack = new Stack<Activity>();
    }

    public void pushActivity(Activity activity) {
        mActivityStack.push(activity);
    }

    public void popActivity() {
        if (mActivityStack.size() > 0) mActivityStack.pop();
    }

    public int getActivitesCount() {
        return mActivityStack.size();
    }

    public void finshAllActivities() {
        for (int i = 0; i < mActivityStack.size(); i++) {
            mActivityStack.get(i).finish();
        }
        mActivityStack.clear();
    }

    public static ActivityStacks getInstance() {
        if (mStacks == null) {
            synchronized (ActivityStacks.class) {
                mStacks = new ActivityStacks();
            }
        }

        return mStacks;
    }
}
