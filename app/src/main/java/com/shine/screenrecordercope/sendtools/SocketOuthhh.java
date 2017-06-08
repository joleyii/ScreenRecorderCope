package com.shine.screenrecordercope.sendtools;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Created by 123 on 2017/5/12.
 */

public class SocketOuthhh {
    ServerSocket serverSocket = null;
    OutputStream outputStream;
    Socket socket;

    private ExecutorService mExecutorService = null;

    public SocketOuthhh() throws IOException {
        try {
            serverSocket = new ServerSocket(9988);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            socket = serverSocket.accept();
                            Log.d("socketsocketsocket", "New aonnection accepted"
                                    + socket.getInetAddress() + ":"
                                    + socket.getPort());
                            outputStream = socket.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] buff, int frameNumber, boolean isKeyFrame) {
        if (outputStream != null) {
            Log.d("currentTimeMillis", System.currentTimeMillis() + "");
            int frameLenth = buff.length;
            int count = buff.length % 1032 == 0 ? buff.length / 1032 : buff.length / 1032 + 1;
            Log.d("contentArray", count + "");
            Log.d("contentArray", buff.length + "");
            int currentIndex = 0;
            for (int i = 0; i < count; i++) {
                if (i != count - 1) {
                    byte[] contentArray = new byte[1032];
                    System.arraycopy(buff, currentIndex * 1032, contentArray, 0, 1032);
                    Log.d("contentArray", contentArray.length + ";;;;" + i);
                    try {
                        byte[] sendArray = new byte[contentArray.length + 20];
                        boolean isLast = i == count - 1;
                        addHeaderMore(sendArray, frameLenth, frameNumber, (short) i, isLast, isKeyFrame, contentArray.length);
                        System.arraycopy(contentArray, 0, sendArray, 20, contentArray.length);
                        writeToSdcard(sendArray, frameNumber, i);
                        outputStream.write(sendArray, 0, sendArray.length);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    int last = frameLenth - currentIndex * 1032;
                    byte[] contentArray = new byte[last];
                    System.arraycopy(buff, currentIndex * 1032, contentArray, 0, last);
                    Log.d("contentArray", contentArray.length + ";;;;" + i);
                    try {
                        byte[] sendArray = new byte[contentArray.length + 20];
                        boolean isLast = i == count - 1;
                        addHeaderMore(sendArray, frameLenth, frameNumber, (short) i, isLast, isKeyFrame, contentArray.length);
                        System.arraycopy(contentArray, 0, sendArray, 20, contentArray.length);
                        writeToSdcard(sendArray, frameNumber, i);
                        outputStream.write(sendArray, 0, sendArray.length);

                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                currentIndex += 1;
            }
        }
    }

    public void addHeaderMore(byte[] b, int frameLenth, int frameNumber, short siliceNumber, boolean isLast, boolean isKeyFrame, int contentArraylength) {
        Log.d("addHeader", frameLenth + ";;"
                + ";frameNumber;" + frameNumber
                + ";siliceNumber;" + siliceNumber
                + ";isLast;" + isLast
                + ";isKeyFrame;" + isKeyFrame
        );

        int lenth = contentArraylength + 16;
        byte[] one = intToByte((0xDAC << 20) + lenth);
        b[0] = one[0];
        b[1] = one[1];
        b[2] = one[2];
        b[3] = one[3];

        b[4] = 'V';
        b[5] = '2';
        b[6] = '0';

        byte[] framLenthArray = intToByte(frameLenth);
        printHexString("framLenthArray", framLenthArray);
        b[8] = framLenthArray[0];
        b[9] = framLenthArray[1];
        b[10] = framLenthArray[2];
        b[11] = framLenthArray[3];

        byte[] frameNumberArray = intToByte(frameNumber);
        printHexString("frameNumberArray", frameNumberArray);
        b[12] = frameNumberArray[0];
        b[13] = frameNumberArray[1];
        b[14] = frameNumberArray[2];
        b[15] = frameNumberArray[3];

        byte[] siliceNumberArray = shortToByte(siliceNumber);
        printHexString("siliceNumberArray", siliceNumberArray);
        b[16] = siliceNumberArray[0];
        b[17] = siliceNumberArray[1];

        if (isLast) {
            b[18] = '1';
        } else {
            b[18] = '0';
        }
        if (isKeyFrame) {
            b[19] = '1';
        } else {
            b[19] = '0';
        }
    }

    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }


    public static byte[] shortToBytes(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }
    //    m_pVideoHeader.type=VIDEO_HDR_CHAR;             'V'       1
//    m_pVideoHeader.ver=VIDEO_HDR_VERSION;           2         1
//    m_pVideoHeader.codec=VICODEC_H264;              0         1
//    m_pVideoHeader.length=0;        整个帧的长度              4
//    m_pVideoHeader.frame=0;         第几帧                   4
//    m_pVideoHeader.slice=0;         第几包                   2
//    m_pVideoHeader.flast=1;         最后一包 1                1
//    m_pVideoHeader.flags = IFlag;   key 1                     1     1048最大  前16位是头  1032

    public static void printHexString(String header, byte[] b) {
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            Log.d("printHexString", header + "::::" + hex.toUpperCase() + "");
        }
    }

    public void addHeader(byte[] b, int frameLenth, int frameNumber, short siliceNumber, boolean isLast, boolean isKeyFrame) {
        Log.d("addHeader", frameLenth + ";;"
                + ";frameNumber;" + frameNumber
                + ";siliceNumber;" + siliceNumber
                + ";isLast;" + isLast
                + ";isKeyFrame;" + isKeyFrame
        );
        b[0] = 'V';
        b[1] = '2';
        b[2] = '0';

        byte[] framLenthArray = intToBytes(frameLenth);
        b[4] = framLenthArray[0];
        b[5] = framLenthArray[1];
        b[6] = framLenthArray[2];
        b[7] = framLenthArray[3];

        byte[] lenthNumberArray = intToBytes(frameNumber);
        b[8] = lenthNumberArray[0];
        b[9] = lenthNumberArray[1];
        b[10] = lenthNumberArray[2];
        b[11] = lenthNumberArray[3];

        byte[] siliceNumberArray = shortToBytes(siliceNumber);
        b[12] = siliceNumberArray[0];
        b[13] = siliceNumberArray[1];

        if (isLast) {
            b[14] = '1';
        } else {
            b[14] = '0';
        }
        if (isKeyFrame) {
            b[15] = '1';
        } else {
            b[15] = '0';
        }

    }

    int number;

    public void writeToSdcard(byte[] keyframe, int frameNumber, int silice) {
        number += 1;
        String path = "mnt/sdcard/" + frameNumber + "_" + silice + "test1.h264";
        Log.d("writeToSdcard", "1111" );
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        Log.d("writeToSdcard", "2222" );
        BufferedOutputStream outputStream1 = null;
        try {
            outputStream1 = new BufferedOutputStream(new FileOutputStream(file));
            Log.d("writeToSdcard", "geiFile: " + keyframe.length);
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
}
