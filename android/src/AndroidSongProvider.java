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
    private Cursor m_allSongCursor;

    public AndroidSongProvider(Activity queryProvider) {
	m_queryProvider = queryProvider;

	//	String[] proj = {MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST};
	String[] proj = {MediaStore.Audio.AudioColumns.ALBUM, 
			 MediaStore.Audio.AudioColumns.ARTIST, 
			 MediaStore.MediaColumns.DATA,
			 BaseColumns._ID};
	// This URI points to the "Artists" table
	//	String uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
	android.net.Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	// Arguments are: table, columns, WHERE clause, WHERE arguments, ORDER clause
	Cursor cursor = m_queryProvider.managedQuery(uri, proj, null, null, null);

	Log.d("MCOTP", new Integer(cursor.getCount()).toString() + " songs in DB");

    }


    // The next two functions give an iterator-like interface
    // and allow the engine to easily cycle over all applicable songs
    public Song getCurrentSong() {
	return new Song("foo","bar","foobar");
    }
    public void advanceSong(){}
    public void toggleRandom() {}
    public boolean isRandom() {return false;}
    public void setGenreClamp(String genreClamp) {}
    public void setBandClamp(String band) {}
    public void setAlbumClamp(String band, String album) {}
    public boolean isBandClamped() {return false;}
    public boolean isAlbumClamped() {return false;}
}
