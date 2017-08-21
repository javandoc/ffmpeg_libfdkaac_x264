package com.guagua.nativeapp.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.guagua.nativeapp.jnibridge.FFmpegJavaNativeBridge;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by android on 8/17/17.
 */

public class NativeAudioRecorder extends BaseMediaRecorder {


    private final IMediaCallback iMediaCallback;
    private AudioRecord mAudioRecord;
    private Thread recordThread;
    private DataOutputStream dataOutputStream;
    private File mDestDir;
    private boolean recording;

    public NativeAudioRecorder(IMediaCallback iMediaCallback) {
        this.iMediaCallback = iMediaCallback;
    }

    public void setPath(String path,String fileName) throws Exception {
        mDestDir = new File(path,fileName);
        if (mDestDir.exists()) {
            mDestDir.delete();
        }
        mDestDir.createNewFile();

    }

    public boolean isRecording() {
        return recording;
    }

    public void stopAudioRecord() {
        if (null != recordThread) {
            recordThread.interrupt();
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordThread = null;
        }
    }

    public void startAudioRecord() {
        synchronized (this) {
            stopAudioRecord();
            int minBufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (AudioRecord.ERROR_BAD_VALUE == minBufferSize) {
                iMediaCallback.onAudioRecordError(BaseMediaRecorder.AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT, "parameters are not supported by the hardware.");
                return;
            }

            FFmpegJavaNativeBridge.prepareInitAACEncode(mDestDir.getAbsolutePath(),"audio.aac");
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);

            if (null == mAudioRecord) {
                iMediaCallback.onAudioRecordError(BaseMediaRecorder.AUDIO_RECORD_ERROR_CREATE_FAILED, "new AudioRecord failed.");
                return;
            }
            recordThread = new Thread() {
                @Override
                public void run() {
                    try {
                        dataOutputStream = new DataOutputStream(new FileOutputStream(mDestDir, true));
                        mAudioRecord.startRecording();
                    } catch (IllegalStateException e) {
                        recording=false;
                        iMediaCallback.onAudioRecordError(BaseMediaRecorder.AUDIO_RECORD_ERROR_UNKNOWN, "startRecording failed.");
                        return;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    byte[] sampleBuffer = new byte[2048];
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            int result = mAudioRecord.read(sampleBuffer, 0, 2048);
                            if (result > 0) {
                                dataOutputStream.write(sampleBuffer, 0, result);
                                recording=true;
                                iMediaCallback.receiveAudioData(sampleBuffer, result);
                            }
                        }
                    } catch (Exception e) {
                        String message = "";
                        if (e != null)
                            message = e.getMessage();
                        recording=false;
                        iMediaCallback.onAudioRecordError(BaseMediaRecorder.AUDIO_RECORD_ERROR_UNKNOWN, message);

                    }
                    try {
                        if (null != dataOutputStream) {
                            dataOutputStream.close();
                            dataOutputStream = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recording=false;
                    mAudioRecord.release();
                    mAudioRecord = null;


                }
            };
            recordThread.start();
        }
    }
}
