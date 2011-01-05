package com.github.tommywalsh.mcotp;

public class AndroidSong implements Song
{
    private String m_uri;
    
    // These can be derived from m_uri, but are cached to save
    // database queries
    private String m_bandName;
    private String m_albumName;
    private String m_songName;

    AndroidSong(String uri, String band, String album, String song) {
	m_uri = uri;
	m_bandName = band;
	m_albumName = album;
	m_songName = song;
    }

    public String uri() {
	return m_uri;
    }
    public String bandName() {
	return m_bandName;
    }
    public String albumName() {
	return m_albumName;
    }
    public String songName() {
	return m_songName;
    }
}
