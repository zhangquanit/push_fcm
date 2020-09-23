package com.sndo.fcm.demo4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sndo.fcm.demo4.util.FcmUtil;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FcmUtil.log("onCreate");
        setContentView(R.layout.activity_main);
        getToken();
        getMsgData();
    }

    /**
     * 获取token
     */
    private void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            FcmUtil.log("----getInstanceId failed,e=" + task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        FcmUtil.log("----getInstanceId,token=" + token);
                        if (!TextUtils.isEmpty(token)) {
                            FcmUtil.saveToken(token);
                        }
                    }
                });
    }

    /**
     * 获取推送消息的data
     */
    private void getMsgData() {
        if (null != getIntent()) {
            Bundle extras = getIntent().getExtras();
            if (null != extras) {
                FcmUtil.log("----点击通知解析data");
                Set<String> keys = extras.keySet();
                for (String str : keys) {
                    FcmUtil.log(str + "=" + extras.getString(str));
                }
            }

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        FcmUtil.log("onNewIntent");
        getMsgData();
    }
}