package com.github.tommywalsh.mcotp;

import java.util.Vector;
import java.lang.String;
import java.io.Serializable;

public class Band implements Serializable {
    private String m_name;
    private int m_firstSong;
    private int m_numSongs;
    private Vector<Album> m_albums;

    public String name() {
	return m_name;
    }

    public int firstSong() {
	return m_firstSong;
    }

    public int numSongs() {
	return m_numSongs;
    }

    public Vector<Album> albums() {
	return m_albums;
    }

    public int lastSong() {
	return m_firstSong + m_numSongs - 1;
    }

    public Band(String name, int firstSong, int numSongs, Vector<Album> albums) {
	m_name = name;
	m_firstSong = firstSong;
	m_numSongs = numSongs;
	m_albums = albums;
    }
	
}
