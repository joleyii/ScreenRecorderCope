package com.shine.screenrecordercope;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import com.example.local.NetworkNative;
import com.shine.screenrecordercope.sendtools.NetAudio;
import com.shine.screenrecordercope.sendtools.SocketOUt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.media.MediaCodec.BUFFER_FLAG_CODEC_CONFIG;


public class RecordService2 extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private int TIMEOUT_USEC = 100000;
    private boolean running;
    private int width = 1366;
    private int height = 768;
    private int dpi;
    boolean isStartThread;
    boolean startRycicle;
    MediaCodec mediaCodec;
    SocketOUt socketOUt;
    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private NetAudio.NetAudioListener mRecoderListener = new NetAudio.NetAudioListener() {
        @Override
        public void onReciver(byte[] bytes) {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    NetworkNative networkNative;

    @Override
    public void onCreate() {
        super.onCreate();
//        audio = new NetAudio();
//        audio.startNet("10.0.1.87", 8000);
        networkNative = new NetworkNative();
        networkNative.OpenSocket();
        try {
//            socketOUt = new SocketOUt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setHW();
        HandlerThread serviceThread = new HandlerThread("service_thread",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    NetAudio audio;

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }
        createfile();
        createVirtualDisplay2();
        running = true;

        startThread();
        return true;
    }

    public byte[] configbyte;
    public int frameNumber = 0;

    private void startThread() {
        if (!isStartThread) {
            Thread EncoderThread = new Thread(new Runnable() {

                @SuppressLint("NewApi")
                @Override
                public void run() {
                    Log.d("EncoderThreadEnco", "EncoderThread");
                    startRycicle = true;
                    byte[] outDataOther;
                    while (startRycicle) {
                        try {
                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            Log.d("outputBufferIndex", outputBufferIndex + "");
//                            while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            Log.d("bufferInfo.size", bufferInfo.size + "");
                            byte[] outData = new byte[bufferInfo.size];
                            outputBuffer.get(outData);
                            if (bufferInfo.flags == BUFFER_FLAG_CODEC_CONFIG) {
                                configbyte = new byte[bufferInfo.size];
                                configbyte = outData;
                            } else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                                byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                                Log.d("tttttttteeeeeee", ";;;;" + outData.length);
//                                    outputStream.write(keyframe, 0, keyframe.length);
//                                    socketOUt.sendData(keyframe, frameNumber, true);
                                outDataOther = keyframe;
                                networkNative.SendFrame(keyframe, keyframe.length, 1);
                                frameNumber += 1;
//                                    geiFile(keyframe);
                            } else if (bufferInfo.flags == -1) {
                                Log.d("tttttttteeeeeee", "=====" + outData.length);
                                networkNative.SendFrame(outData, outData.length, 0);
                            } else {
                                Log.d("tttttttteeeeeee", "[[[[[" + outData.length);
//                                    outputStream.write(outData, 0, outData.length);
//                                    socketOUt.sendData(outData, frameNumber, false);
                                networkNative.SendFrame(outData, outData.length, 0);
                                frameNumber += 1;
//                                    geiFile(outData);
                            }
                            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
//                            }

                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            });
            EncoderThread.start();
        }

    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }
        running = false;

        virtualDisplay.release();
        mediaProjection.stop();

        return true;
    }

    public class RecordBinder extends Binder {
        public RecordService2 getRecordService() {
            return RecordService2.this;
        }
    }

    private void createVirtualDisplay2() {
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 10);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mediaCodec.createInputSurface();
            mediaCodec.start();
            virtualDisplay =
                    mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                            surface,
                            null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedOutputStream outputStream;

    private void createfile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + refFormatNowDate() + "aaaaaaaa.h264";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("HH_mm_ss");
        String retStrFormatNowDate = sdFormatter.format(nowTime);

        return retStrFormatNowDate;
    }

    public void setHW() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        height = dm.widthPixels;
        width = dm.heightPixels;
        dpi = dm.densityDpi;
    }
}