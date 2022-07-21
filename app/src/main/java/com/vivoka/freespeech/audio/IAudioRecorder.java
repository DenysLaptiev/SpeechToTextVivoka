package com.vivoka.freespeech.audio;

public interface IAudioRecorder {
    void onAudioData(short[] buffer, int amplitude);
}
