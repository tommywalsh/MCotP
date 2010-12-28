/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 */
package com.github.tommywalsh.mcotp;

import android.media.MediaPlayer;
import java.util.HashSet;
import java.util.Iterator;


// This class is a simple adapter that takes commands from a generic (not Android-specific) Engine
// object, and relays/translates them to an Android MediaPlayer object
public class AndroidPlayer implements Player
{

    private HashSet<Player.SongFinishedListener> m_listeners = new HashSet<Player.SongFinishedListener>();

    AndroidPlayer() {
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
	    m_player.setDataSource(song.songName());
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
