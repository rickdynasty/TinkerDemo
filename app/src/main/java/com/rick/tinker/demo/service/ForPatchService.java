package com.rick.tinker.demo.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.rick.tinker.demo.util.Utils;
import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;

/**
 * 处理加载补丁结果
 * 在成功加载热更新插件后，TinkerInstaller回调提示更新成功，并且可以这里做一些项目的需求相关操作
 */
public class ForPatchService extends DefaultTinkerResultService {
    private static final String TAG = "Tinker.ForPatchService";


    @Override
    public void onPatchResult(final PatchResult result) {
        if (result == null) {
            TinkerLog.e(TAG, "ForPatchService received null result!!!!");
            return;
        }
        TinkerLog.i(TAG, "ForPatchService receive result: %s", result.toString());

        //first, we want to kill the recover process
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result.isSuccess) {
                    Toast.makeText(getApplicationContext(), "patch success, please restart process", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "patch fail, please check reason", Toast.LENGTH_LONG).show();
                }
            }
        });
        // is success and newPatch, it is nice to delete the raw file, and restart at once
        // for old patch, you can't delete the patch file
        if (result.isSuccess) {
            File rawFile = new File(result.rawPatchFilePath);
            if (rawFile.exists()) {
                TinkerLog.i(TAG, "save delete raw patch file");
                SharePatchFileUtil.safeDeleteFile(rawFile);
            }
            //not like TinkerResultService, I want to restart just when I am at background!
            //if you have not install tinker this moment, you can use TinkerApplicationHelper api
            if (checkIfNeedKill(result)) {
                if (Utils.isBackground()) {
                    TinkerLog.i(TAG, "it is in background, just restart process");
                    restartProcess();
                } else {
                    //we can wait process at background, such as onAppBackground
                    //or we can restart when the screen off
                    TinkerLog.i(TAG, "tinker wait screen to restart process");
                    new ScreenState(getApplicationContext(), new ScreenState.IOnScreenOff() {
                        @Override
                        public void onScreenOff() {
                            restartProcess();
                        }
                    });
                }
            } else {
                TinkerLog.i(TAG, "I have already install the newly patch version!");
            }
        }
    }

    /**
     * you can restart your process through service or broadcast
     */
    private void restartProcess() {
        TinkerLog.i(TAG, "app is background now, i can kill quietly");
        //you can send service or broadcast intent to restart your process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    static class ScreenState {
        interface IOnScreenOff {
            void onScreenOff();
        }

        ScreenState(Context context, final IOnScreenOff onScreenOffInterface) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            context.registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent in) {
                    String action = in == null ? "" : in.getAction();
                    TinkerLog.i(TAG, "ScreenReceiver action [%s] ", action);
                    if (Intent.ACTION_SCREEN_OFF.equals(action)) {

                        context.unregisterReceiver(this);

                        if (onScreenOffInterface != null) {
                            onScreenOffInterface.onScreenOff();
                        }
                    }
                }
            }, filter);
        }
    }

}