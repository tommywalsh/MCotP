package com.github.tommywalsh.mcotp;

import android.app.Activity;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;

import android.util.Log;


public class AndroidSongProvider implements SongProvider
{
    private Activity m_queryProvider;
    private Cursor m_cursor;

    

    public AndroidSongProvider(Activity queryProvider) {
	m_queryProvider = queryProvider;

	String[] proj = {MediaStore.Audio.AudioColumns.ALBUM, 
			 MediaStore.Audio.AudioColumns.ALBUM_ID, 
			 MediaStore.Audio.AudioColumns.ARTIST, 
			 MediaStore.Audio.AudioColumns.ARTIST_ID, 
			 MediaStore.MediaColumns.DATA,
			 BaseColumns._ID};

	android.net.Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	m_cursor = m_queryProvider.managedQuery(uri, proj, null, null, null);

	Log.d("MCOTP", new Integer(m_cursor.getCount()).toString() + " songs in DB");
	m_cursor.moveToFirst();
    }


    // The next two functions give an iterator-like interface
    // and allow the engine to easily cycle over all applicable songs
    public Song getCurrentSong() {
	return new Song(m_cursor.getString(2),
			m_cursor.getString(0),
			m_cursor.getString(4));
    }
    public void advanceSong() {
	m_cursor.moveToNext();
	if (m_cursor.isAfterLast()) {
	    m_cursor.moveToFirst();
	}
    }
    public void toggleRandom() {}
    public boolean isRandom() {return false;}
    public void setGenreClamp(String genreClamp) {}
    public void setBandClamp(String band) {}
    public void setAlbumClamp(String band, String album) {}
    public boolean isBandClamped() {return false;}
    public boolean isAlbumClamped() {return false;}
}
