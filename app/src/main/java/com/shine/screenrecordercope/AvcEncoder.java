package com.shine.screenrecordercope;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import com.shine.screenrecordercope.sendtools.SocketOUt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;


public class AvcEncoder {
    private final static String TAG = "MeidaCodec";

    private int TIMEOUT_USEC = 12000;

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;
    byte[] m_info = null;
    int number = 0;

    public byte[] configbyte;


    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int framerate) {

        m_width = width;
        m_height = height;
        m_framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        createfile();
    }

    private BufferedOutputStream outputStream;
    FileOutputStream outStream;

    private void createfile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + refFormatNowDate() + ".h264";
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

    public void geiFile(byte[] keyframe) {
        number += 1;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/6/" + number + "test1.h264";
        File file = new File(path);
        BufferedOutputStream outputStream1 = null;
        try {
            outputStream1 = new BufferedOutputStream(new FileOutputStream(file));
            Log.d("rerwqqqqwww", "geiFile: " + keyframe.length);
            outputStream1.write(keyframe, 0, keyframe.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream1 != null) {
                try {
                    outputStream1.flush();
                    outputStream1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void StopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;

    public boolean isRuning = false;

    public void StopThread() {
        isRuning = false;
        try {
            StopEncoder();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    int count = 0;
    int frameNumber = 0;
    SocketOUt socketOUt;
    public void StartEncoderThread() {
        InputStream input = null;
        final byte[] tempbytes = new byte[460800];
        try {
            int byteread = 0;
            input = new FileInputStream("mnt/sdcard/aaa.h");
            while ((byteread = input.read(tempbytes)) != -1) {
                System.out.write(tempbytes, 0, byteread);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {

                }
            }
        }

        Flowable
                .interval(45, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        isRuning = true;
                        byte[] input = null;
                        long pts = 0;
                        long generateIndex = 0;
                        if (input != null) {
                            try {
                                long startMs = System.currentTimeMillis();
                                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                                Log.d("bufferInfo.size", inputBufferIndex + "");
                                if (inputBufferIndex >= 0) {
                                    pts = computePresentationTime(generateIndex);
                                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                    inputBuffer.clear();
                                    inputBuffer.put(input);
                                    Log.d("input.length", input.length + "");
                                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                    generateIndex += 1;
                                }

                                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                                while (outputBufferIndex >= 0) {
                                    //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
                                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                    Log.d("bufferInfo.size", bufferInfo.size + "");
                                    byte[] outData = new byte[bufferInfo.size];
                                    outputBuffer.get(outData);
                                    frameNumber += 1;
                                    if (bufferInfo.flags == 2) {
                                        configbyte = new byte[bufferInfo.size];
                                        configbyte = outData;
                                    } else if (bufferInfo.flags == 1) {
                                        byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                                        Log.d("keyframe.length", keyframe.length + "");
                                        socketOUt.sendData(outData, frameNumber, false);
//                                    outputStream.write(keyframe, 0, keyframe.length);
//                                    geiFile(keyframe);
                                    } else {
                                        Log.d("outData.length", outData.length + "");
//                                    outputStream.write(outData, 0, outData.length);
                                        socketOUt.sendData(outData, frameNumber, false);
                                    }
                                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                                }

                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        } else {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }

    public String refFormatNowDate() {
        Date nowTime = new Date(System.currentTimeMillis());
        SimpleDateFormat sdFormatter = new SimpleDateFormat("HH_mm_ss");
        String retStrFormatNowDate = sdFormatter.format(nowTime);

        return retStrFormatNowDate;
    }

}