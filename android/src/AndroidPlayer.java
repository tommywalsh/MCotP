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

    // Here's the Android media player we'll be wrapping
    private MediaPlayer m_wrappedPlayer;


    // There are two kinds of listening going on here.  First, this class needs to listen
    // to the wrapped player so that we know when it's done playing.
    // Second, the generic engine will be listening to this class to know when we are 
    // done playing.  
    // Essentially, we're a middleman listener, and just need to relay the messages


    // The engine will install listeners.  Keep track of them here
    private HashSet<Player.SongFinishedListener> m_listeners = new HashSet<Player.SongFinishedListener>();
    
    public void addListener(Player.SongFinishedListener l) {
	m_listeners.add(l);
    }

    public void removeListener(Player.SongFinishedListener l) {
	m_listeners.remove(l);
    }



    // Here's the helper class we'll use to listen to the wrapped player
    private class CListener implements MediaPlayer.OnCompletionListener {
	public void onCompletion(MediaPlayer p) {
	    for (Iterator<Player.SongFinishedListener> iter = m_listeners.iterator(); iter.hasNext();  ) {
		iter.next().onSongFinished();
	    }	
	}
    }




    AndroidPlayer() {
	// Make a new Android MediaPlayer and start listening to it
	m_wrappedPlayer = new MediaPlayer();
	m_wrappedPlayer.setOnCompletionListener(new CListener());
    }




    // The rest of these methods simply relay commands from the generic
    // engine to the Android media player
    public void play() {
	m_wrappedPlayer.start();
    }

    public void pause() {
	m_wrappedPlayer.pause();
    }

    public void restartSong() {
	m_wrappedPlayer.start();
    }

    public void setSong(Song song) {
	m_wrappedPlayer.reset();
	try {
	    m_wrappedPlayer.setDataSource(song.uri());
	    m_wrappedPlayer.prepare();
	} catch (java.io.IOException e) {
	}
    }

}
