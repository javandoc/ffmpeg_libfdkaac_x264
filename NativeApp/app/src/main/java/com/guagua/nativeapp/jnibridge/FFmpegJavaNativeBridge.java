package com.guagua.nativeapp.jnibridge;

/**
 * Created by android on 8/17/17.
 */

public class FFmpegJavaNativeBridge {


    /**
     * 编码一帧音频
     * @param data
     * @return
     */
    public static native int encode2AAC(byte []data);

    /**
     * 编码一帧视频
     * @param data
     * @return
     */
    public static native int encodeFrame2H264(byte[] data);

    public static native int prepareInitAACEncode(String path,String fileName);


    public static native int prepareInitFFmpegEncode(String basePath);


    public static native int endRecord();

    public static native int releaseRecord();




}
