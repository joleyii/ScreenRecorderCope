package com.shine.screenrecordercope.sendtools;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class AudioReiveHeaderUtil {
    private static final String TAG = "AudioReiveHeaderUtil";

    public int recieveLength(byte[] buffer) {
        byte[] tcpSys = new byte[4];
        tcpSys[0] = buffer[0];
        tcpSys[1] = buffer[1];
        tcpSys[2] = buffer[2];
        tcpSys[3] = buffer[3];
        int headerl = bytesToInt(tcpSys);
        int Offset = (0xDAC << 20);
        headerl = headerl - Offset;
        return headerl;
    }
  /*  public void initHeader(byte[] header,byte[] buffer){
        byte[] headByte= new byte[4];
        byte[] count= new byte[4];
        byte[] frameByte= new byte[4];
        byte[] sliceByte= new byte[2];
        int headerl=bytesToInt(header);
        int Offset =(0xDAC << 20);
        headerl = headerl-Offset;
        if(headerl!=972){
            Log.e(TAG,"headerl!=972 headerl  "+headerl);
            Log.e(TAG,"headerl!=972 header  "+Arrays.toString(header));
            Log.e(TAG,"headerl!=972 buffer  "+Arrays.toString(buffer));
        }
        count[0]  = header[12];
        count[1]  = header[13];
        count[2] = header[14];
        count[3] = header[15];

        int countl=bytesToInt(count);
    }*/

    public byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];

        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8;
        }

        return b;
    }

    public byte[] toByteArray(int iSource) {
        byte[] bLocalArr = new byte[4];

        for (int i = 0; i < 4; i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }

        return bLocalArr;
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

    public short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);

        s1 <<= 8;
        s = (short) (s0 | s1);

        return s;
    }


    public int bytesToInt(byte[] bytes) {
        int addr = bytes[0] & 0xFF;

        addr |= ((bytes[1] << 8) & 0xFF00);
        addr |= ((bytes[2] << 16) & 0xFF0000);
        addr |= ((bytes[3] << 24) & 0xFF000000);

        return addr;
    }

    // char转byte

    private byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);

        return bb.array();

    }

// byte转char

    private char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);

        return cb.array();
    }

    public static char byteToChar(byte[] b) {
        char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
        return c;
    }

    /**
     * 将byte[2]转换成short
     *
     * @param b
     * @return
     */
    public short byte2Short(byte[] b) {
        return (short) ((b[0] & 0xff) | (b[1] << 8));
    }

}
