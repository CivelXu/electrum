package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class TouchHardwareActivity extends AppCompatActivity {
    public final static String FROM = "org.haobtc.wallet.from";
    private String tag;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch);
        CommonUtils.enableToolBar(this, 0);
        tag = getIntent().getStringExtra(FROM);
        initNFC();

    }

    private void initNFC() {
        //处理接收到的消息的方法
        new Handler(arg0 -> {
            //实现页面跳转
            startNewPage();
            return false;
        }).sendEmptyMessageDelayed(0, 5000);

    }

    private void startNewPage() {
        Intent intent = new Intent(this, PinSettingActivity.class);
        intent.putExtra(FROM, tag);
        startActivity(intent);
    }

}