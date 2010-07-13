package com.github.tommywalsh.mcotp;

import java.util.HashSet;
import java.util.Iterator;

public class Engine
{
    private SongProvider m_provider;
    private Player m_player;
    private Song m_currentSong;
    private boolean m_isPlaying;
    private HashSet<UpdateListener> m_listeners;

    public interface UpdateListener {
	public void onSongChanged(Song song);
    }

    public Engine(SongProvider provider, Player player) {
	m_listeners = new HashSet<UpdateListener>();
	m_provider = provider;
	m_player = player;
	m_isPlaying = false;
	m_currentSong = m_provider.getCurrentSong();
	m_player.setSong(m_currentSong);
	m_player.addListener(new Player.SongFinishedListener() {
		public void onSongFinished() {
		    nextSong();
		}
	    });
    }
    
    public void addListener(UpdateListener l) {
	m_listeners.add(l);
    }

    public Song getSong() {
	return m_currentSong;
    }

    private void notifySongChanged(Song newSong) {
	for (Iterator<UpdateListener> iter = m_listeners.iterator(); iter.hasNext();  ) {
	    iter.next().onSongChanged(newSong);
	}	

    }

    public void toggleRandom() {
	m_provider.toggleRandom();
    }

    public boolean isRandom() {
	return m_provider.isRandom();
    }

    public void setClamp(String genreClamp, String bandClamp, String albumClamp) {
	if (albumClamp != null) {
	    assert bandClamp != null;
	    m_provider.setAlbumClamp(bandClamp, albumClamp);
	} else if (bandClamp != null) {
	    m_provider.setBandClamp(bandClamp);
	} else {
	    assert genreClamp != null;
	    m_provider.setGenreClamp(genreClamp);
	}
	Song song = m_provider.getCurrentSong();
	if (!song.equals(m_currentSong)) {
	    nextSong();
	}
    }

    public void play() {
	if (!m_isPlaying) {
	    m_isPlaying = true;
	    m_player.play();
	}
    }
    public void pause() {
	if (m_isPlaying) {
	    m_isPlaying = false;
	    m_player.pause();
	}
    }

    public boolean isPlaying() {
	return m_isPlaying;
    }

    public void togglePlayPause() {
	if (m_isPlaying) {
	    pause();
	} else {
	    play();
	}
    }


    public void restartSong() {
	m_player.restartSong();
    }
   
    public void nextSong() {
	m_provider.advanceSong();
	Song song = m_provider.getCurrentSong();
	m_player.pause();
	m_player.setSong(song);
	if (m_isPlaying) {
	    m_player.play();
	}
        m_currentSong = song;
	notifySongChanged(song);
    }
}
