package com.shine.screenrecordercope.sendtools;

public class AudioPackHeaderUtil {
    public long count;

    private AudioPackHeader initHeader(int headerCount) {
        AudioPackHeader header = new AudioPackHeader();

        header.type = 'U';
        header.ver = 2;
        header.codec = 0;
        header.freq = (short) 16000;
        header.extra = 0;
        header.chl = 1;
        header.bits = 16;
        header.count = headerCount;

        return header;
    }

    public byte[] initAudio(int length, int headerCount, byte[] s) {
        AudioPackHeader header = initHeader(headerCount);
        byte[] one = intToByte((0xDAC << 20) + length);
        s[0] = one[0];
        s[1] = one[1];      // AUDIO_HDR_VERSION 2
        s[2] = one[2];   // AUDIO_HDR_VERSION 2
        s[3] = one[3];    // AUDIO_HDR_VERSION 2


        s[4] = header.type;
        s[5] = header.ver;      // AUDIO_HDR_VERSION 2
        s[6] = header.codec;    // AUDIO_HDR_VERSION 2
        s[7] = header.extra;    // AUDIO_HDR_VERSION 2

        byte[] iB = shortToByte(header.freq);

        s[8] = iB[0];
        s[9] = iB[1];
        s[10] = header.chl;    // AUDIO_HDR_VERSION 2
        s[11] = header.bits;

        byte[] b = intToByte(header.count);

        s[12] = b[0];
        s[13] = b[1];
        s[14] = b[2];
        s[15] = b[3];

        return s;
    }

    public byte[] initAudio(int length, int headerCount) {
        byte[] s = new byte[16];
        AudioPackHeader header = initHeader(headerCount);
        byte[] one = intToByte((0xDAC << 20) + length);
        s[0] = one[0];
        s[1] = one[1];      // AUDIO_HDR_VERSION 2
        s[2] = one[2];   // AUDIO_HDR_VERSION 2
        s[3] = one[3];    // AUDIO_HDR_VERSION 2


        s[4] = header.type;
        s[5] = header.ver;      // AUDIO_HDR_VERSION 2
        s[6] = header.codec;    // AUDIO_HDR_VERSION 2
        s[7] = header.extra;    // AUDIO_HDR_VERSION 2

        byte[] iB = shortToByte(header.freq);

        s[8] = iB[0];
        s[9] = iB[1];
        s[10] = header.chl;    // AUDIO_HDR_VERSION 2
        s[11] = header.bits;

        byte[] b = intToByte(header.count);

        s[12] = b[0];
        s[13] = b[1];
        s[14] = b[2];
        s[15] = b[3];

        return s;
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
}
