package com.shine.screenrecordercope.sendtools;

public class AudioPackHeader {
    public byte type;     // AUDIO_HDR_CHAR  2
    public byte ver;      // AUDIO_HDR_VERSION 2
    public byte codec;    // AUDIO_CODEC_ID 2
    public byte extra;    // 2
    public short freq;     // 48000 4
    public byte chl;      // 1 , 2 2
    public byte bits;     // 16 2 8
    public int count;
}
