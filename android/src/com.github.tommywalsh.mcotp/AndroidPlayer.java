package com.github.tommywalsh.mcotp;

import mcotp.*;
import android.media.MediaPlayer;
import java.util.HashSet;
import java.util.Iterator;

public class AndroidPlayer implements Player
{
    StorageProvider m_storage;

    private HashSet<Player.SongFinishedListener> m_listeners = new HashSet<Player.SongFinishedListener>();

    AndroidPlayer(StorageProvider sp) {
	m_storage = sp;
	m_player = new MediaPlayer();
	m_player.setOnCompletionListener(new CListener());
    }


    public void addListener(Player.SongFinishedListener l) {
	m_listeners.add(l);
    }

    public void removeListener(Player.SongFinishedListener l) {
	m_listeners.remove(l);
    }


    private MediaPlayer m_player;

    public void play() {
	m_player.start();
    }

    public void pause() {
	m_player.pause();
    }

    public void restartSong() {
	m_player.start();
    }

    public void setSong(Song song) {
	m_player.reset();
	try {
	    m_player.setDataSource(m_storage.getSongPath(song));
	    m_player.prepare();
	} catch (java.io.IOException e) {
	}
    }

    private class CListener implements MediaPlayer.OnCompletionListener {
	public void onCompletion(MediaPlayer p) {
	    for (Iterator<Player.SongFinishedListener> iter = m_listeners.iterator(); iter.hasNext();  ) {
		iter.next().onSongFinished();
	    }	
	}
    }
}
