package com.github.tommywalsh.mcotp.android;

import com.github.tommywalsh.mcotp.shared.*;

import android.app.Activity;
import android.os.Bundle;
import java.util.Vector;
import android.widget.TextView;


public class AndroidUI extends Activity
{
    private AndroidPlayer m_player;
    private SongProvider m_songProvider;
    private TextView m_songText;
    private Engine m_engine;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	snagUIElements();

	initializeInternals();

    }

    private void snagUIElements() {
	m_songText = (TextView) findViewById(R.id.songText);
    }
    
    private void initializeInternals() {
	PosixStorageProvider psp = new PosixStorageProvider("/sdcard/music");
	m_songProvider = new SongProvider(psp);
	m_songProvider.constructLibrary();

	m_player = new AndroidPlayer(psp);
	m_engine = new Engine(m_songProvider, m_player);

	m_engine.addListener(new Engine.UpdateListener() {
		public void onSongChanged(Song newSong) {
		    updateSongDisplay(newSong);
		}
	    });
    }

    // should instead restore state from previous session!
    private void startPlaying() {
	// should be controlled by UI
	m_engine.toggleRandom();
	m_engine.nextSong();
	
	m_engine.togglePlayPause();

	updateSongDisplay(m_engine.getSong());
    }

    private void updateSongDisplay(Song song) {
	m_songText.setText(song.songName());
    }
}
