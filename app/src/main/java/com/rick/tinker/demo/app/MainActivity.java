package com.rick.tinker.demo.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rick.tinker.demo.R;
import com.rick.tinker.demo.util.Utils;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "rick_Print:MainActivity";
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_load).setOnClickListener(this);
        findViewById(R.id.btn_kill).setOnClickListener(this);
        findViewById(R.id.btn_test_func).setOnClickListener(this);

        title = (TextView) findViewById(R.id.tv_title);
//        title.setText(R.string.test_des);
        title.setText(R.string.test_des2);

        checkWriteExternalStoragePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.setBackground(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.setBackground(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_load:
                loadPatch();
                break;
            case R.id.btn_kill:
                killApp();
            case R.id.btn_test_func:
                //仅用于功能测试
                testFunc();
                break;
            default:
                break;
        }
    }

    private void testFunc() {
//        Toast.makeText(this, getString(R.string.test_des), Toast.LENGTH_LONG).show();
        Toast.makeText(this, getString(R.string.test_des2), Toast.LENGTH_LONG).show();
    }

    /**
     * 加载热补丁插件
     */
    public void loadPatch() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tinker/patch_signed_7zip.apk";
        TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), path);
    }

    /**
     * 杀死应用加载补丁
     */
    public void killApp() {
        ShareTinkerInternals.killAllOtherProcess(getApplicationContext());
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void checkWriteExternalStoragePermission() {
        //6.0以上的有这个权限问题
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            int REQUEST_EXTERNAL_STORAGE = 1;
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }
}
