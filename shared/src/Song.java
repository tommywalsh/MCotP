package com.github.tommywalsh.mcotp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Song
{
    private String m_band;
    private String m_album;
    private String m_songFile;

    public Song(String band, String album, String songFile) {
        m_band = band;
        m_album = album;
        m_songFile = songFile;
    }

    static private boolean strEq(String a, String b) {
        boolean eq = false;
        if (a == null) {
            eq = (b == null);
        } else {
            if (b != null) {
                eq = (a.equals(b));
            }
        }
        return eq;
    }

    public boolean equals(Object o) {
        boolean iseq = false;
        if (o instanceof Song) {
            Song other = (Song)o;
            iseq = 
                strEq(m_band, other.m_band) &&
                strEq(m_album, other.m_album) &&
                strEq(m_songFile, other.m_songFile);
        }
        return iseq;
    }
    
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + (m_band == null ? 0 : m_band.hashCode());
        hash = hash * 31 + (m_album == null ? 0 : m_album.hashCode());
        hash = hash * 31 + (m_songFile == null ? 0 : m_songFile.hashCode());
        return hash;
    }

    public String bandName() {
        return m_band;
    }

    public String albumName() {
        return m_album;
    }

    public String songName() {
        return m_songFile;
    }

    private static Pattern s_extRE = Pattern.compile("\\..*$");
    private static Pattern s_idxRE = Pattern.compile("^\\d+ *- *");
    public String songDisplayName() {
	Matcher m = s_extRE.matcher(songName());
	String noext = m.replaceFirst("");
	m = s_idxRE.matcher(noext);
	return m.replaceFirst("");
    }
}
