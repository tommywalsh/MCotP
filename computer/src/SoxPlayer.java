package com.github.tommywalsh.mcotp;

import java.lang.Process;
import java.lang.Runtime;
import java.io.IOException;
import java.lang.Thread;
import java.util.HashSet;
import java.util.Iterator;

public class SoxPlayer implements Player, ExecThread.FinishedListener
{
    private final String c_playCommand = "play";
    private ExecThread m_thread;
    private Song m_song;
    private HashSet<Player.SongFinishedListener> m_listeners = new HashSet<Player.SongFinishedListener>();
    private PosixStorageProvider m_storage;

    SoxPlayer(PosixStorageProvider sp) {
	m_storage = sp;
    }

    public void onFinished() {
	for (Iterator<Player.SongFinishedListener> iter = m_listeners.iterator(); iter.hasNext();  ) {
	    iter.next().onSongFinished();
	}	
    }

    public void addListener(Player.SongFinishedListener l) {
	m_listeners.add(l);
    }

    public void removeListener(Player.SongFinishedListener l) {
	m_listeners.remove(l);
    }

    public void play() {
	String[] ca = new String[3];
	ca[0] = c_playCommand;
	ca[1] = "-q";
	ca[2] = m_storage.getSongPath(m_song);
	m_thread = new ExecThread(ca, this);
	m_thread.start();
    }

    private void stopCold() {
	if (m_thread != null) {
	    m_thread.interrupt();
	    try {
		m_thread.join();
	    } catch (InterruptedException e) {
	    }
	    m_thread = null;
	}
    }

    public void pause() {
	// pausing not implemented yet.  But, we can stop cold.
	stopCold();
    }

    public void restartSong() {
	stopCold();
	play();
    }


    public void setSong(Song song) {
	stopCold();
	m_song = song;
    }

}
