package com.github.tommywalsh.mcotp;

import java.lang.String;
import java.io.Serializable;

public class Album implements Serializable {
    private String m_name;
    private int m_firstSong;
    private int m_numSongs;

    public int firstSong() {
	return m_firstSong;
    }

    public int numSongs() {
	return m_numSongs;
    }

    public int lastSong() {
	return m_firstSong + m_numSongs - 1;
    }

    public String name() {
	return m_name;
    }

    public Album(String name, int firstSong, int numSongs) {
	m_name = name;
	m_firstSong = firstSong;
	m_numSongs = numSongs;
    }
}

