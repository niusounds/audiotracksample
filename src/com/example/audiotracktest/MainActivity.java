package com.example.audiotracktest;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {
	private static final int SAMPLE_RATE = 44100;
	private static final float BUFFER_MULTIPLE = 1; // 最小バッファーサイズより何倍するか
	private AudioTrack audio;
	private AudioRecord record;
	private boolean running;
	private short[] buffer;

	@AfterInject
	void init() {
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		int outBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		int inBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		int bufferSize = (int) (Math.max(outBufferSize, inBufferSize) * BUFFER_MULTIPLE);
		buffer = new short[bufferSize];
	}

	@Override
	protected void onPause() {
		running = false;
		super.onPause();
	}

	@Background
	void loop() {
		if (running) {
			return;
		}

		int bufferSize = buffer.length;
		audio = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
		record = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		audio.play();
		record.startRecording();

		running = true;
		try {
			while (running) {
				int len = record.read(buffer, 0, bufferSize);
				if (len > 0) {
					audio.write(buffer, 0, len);
				}
			}
		} finally {
			// release audio
			audio.stop();
			audio.release();
			record.stop();
			record.release();
			audio = null;
			record = null;
		}
	}

	@Click(R.id.button1)
	void toggleAudioEngine() {
		if (running) {
			running = false;
		} else {
			loop();
		}
	}
}
