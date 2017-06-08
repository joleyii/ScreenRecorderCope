package com.shine.screenrecordercope;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.local.NetworkNative;
import com.shine.screenrecordercope.sendtools.SocketOuthhh;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.functions.Consumer;

/**
 * Created by 123 on 2017/5/18.
 */

public class MainActivity3 extends Activity {
    int frameNumber = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.INTERNET
                        , Manifest.permission.ACCESS_WIFI_STATE
                        , Manifest.permission.ACCESS_NETWORK_STATE
                        , Manifest.permission.CHANGE_WIFI_STATE
                        , Manifest.permission.CHANGE_NETWORK_STATE
                )
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                });
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        SocketOuthhh socketOuthhh = null;
        try {
            socketOuthhh = new SocketOuthhh();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream in = null;
        final byte[] tempbytes = new byte[216480];
        try {
            int byteread = 0;
            in = new FileInputStream("mnt/sdcard/aaa.h264");
            while ((byteread = in.read(tempbytes)) != -1) {
                Log.d("byteread", "byteread");
                System.out.write(tempbytes, 0, byteread);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }

        final NetworkNative networkNative = new NetworkNative();
        networkNative.OpenSocket();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    networkNative.SendFrame(tempbytes, tempbytes.length, 1);
                }
            }
        }).start();
    }
}
