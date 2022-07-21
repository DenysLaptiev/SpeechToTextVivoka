package com.vivoka.freespeech.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vivoka.vsdk.audio.ProducerModule;
import com.vivoka.vsdk.util.BufferUtils;

public class AudioRecorder extends ProducerModule {

    private final String TAG = "AudioRecorder";

    private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int SAMPLE_RATE = 16000;
    private final int SAMPLE_PER_CHANNEL = 1024;

    private AudioRecord _recorder;
    private Thread _recorderThread;

    private volatile boolean _isRunning = false;
    public boolean isRunning() {
        return _isRunning;
    }

    private void recorderThread() {
        if (_recorder == null || _recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord could not be initialized");
            return;
        }

        _recorder.startRecording();
        _isRunning = true;

        while (_recorder != null && _isRunning) {
            short[] buffer = new short[SAMPLE_PER_CHANNEL];
            int audioData = _recorder.read(buffer, 0, SAMPLE_PER_CHANNEL);

            // Calc amplitude of the wav
            int amplitude = 0;
            for (int i = 0; i < audioData / 2; i++) {
                short sample = buffer[i * 2];
                if (sample > amplitude) {
                    amplitude = sample;
                }
            }

            try {
                dispatchAudio(1, SAMPLE_RATE, BufferUtils.convertShortsToBytes(buffer), !_isRunning);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean open() { return true; }

    @Override
    public boolean run() { return false; }

    @SuppressLint("MissingPermission")
    @Override
    public boolean start() {
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
        try {
            _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
            if (_recorder.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord could not be initialized");
                return false;
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        _recorderThread = new Thread(this::recorderThread);
        _recorderThread.start();
        return true;
    }

    @Override
    public boolean stop() {
        _isRunning = false;

        if (_recorder != null && _recorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
            _recorder.stop();
            _recorder.release();
        }

        if (_recorderThread != null) {
            _recorderThread.interrupt();
        }

        return true;
    }
}
