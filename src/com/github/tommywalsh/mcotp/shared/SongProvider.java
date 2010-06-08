package com.github.tommywalsh.mcotp.shared;

import java.util.Vector;
import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Random;
/////////////////////////////////////////////////////////////////////
//
// This class is in charge of sequencing songs and providing them
// to the engine.  The public interface is documented below
//
/////////////////////////////////////////////////////////////////////


// TO-DO List
//
// * Serialize our cache of songs to speed loading
//


public class SongProvider
{
    // The next two functions give an iterator-like interface
    // and allow the engine to easily cycle over all applicable songs
    public Song getCurrentSong() {
	String albumName = null;
	if (m_currentAlbum != null) {
	    albumName = m_currentAlbum.name;
	}
	return new Song(m_currentBand.name, albumName, m_currentSong);
    }

    public void advanceSong() {
	if (m_isRandom) {
	    advanceToRandomSong();
	} else {
	    advanceToNextLinearSong();
	}
    }
    
    //  Should the songs be provided in random order, or sequential?
    public void toggleRandom() {
	m_isRandom = !m_isRandom;
    }

    // The "clamp" functions specify the "boundaries" of available songs.
    // These are currently unimplemented.  In future, this might be
    // expanded to allow arbitrary filters 
    // (e.g. "Allow only death metal or Elton John or Spirit in the Sky")
    
    // Only serve up songs of the given genre
    public void setGenreClamp(String genreClamp)
    {
        m_bandClamp = "";
        m_albumClamp = "";
    }

    // Only serve up songs from a single band
    public void setBandClamp(String band)
    {
        m_bandClamp = band;
        m_albumClamp = "";
    }

    // Only serve up songs from a single album
    public void setAlbumClamp(String band, String album)
    {
        m_bandClamp = band;
        m_albumClamp = album;
    }

    public SongProvider(StorageProvider sp) {
	m_storage = sp;
    }



    ///////// END OF PUBLIC INTERFACE //////////












    private StorageProvider m_storage;
    
    // might be better to store indicies in tree, instead of bandinfos
    private TreeMap<Integer, Integer> m_indexToBand; 
    private Vector<BandInfo> m_allBands;
    private int m_numSongs;
    private boolean m_isRandom = false;
    private String m_bandClamp = "";
    private String m_albumClamp = "";
    private String m_currentSong;
    private int m_currentSongNumber;
    
    private BandInfo m_currentBand;
    private AlbumInfo m_currentAlbum;







    ////////////////////////////////////////////////////////////
    // Convenience function wrapping storage provider methods //
    ////////////////////////////////////////////////////////////
    private Vector<String> getFiles(String path) {
	return m_storage.getFilesOrDirs(path, true);
    }

    private Vector<String> getDirectories(String path) {
	return m_storage.getFilesOrDirs(path, false);
    }

    private Vector<String> getAlbumSongs(String band, String album) {	
	return getFiles(m_storage.getAlbumPath(band, album));
    }

    private Vector<String> getAlbums(String band) {
	return getDirectories(m_storage.getBandPath(band));
    }

    private Vector<String> getLooseSongs(String band) {
	return getFiles(m_storage.getBandPath(band));
    }

    private Vector<String> getAllBands() {
	return getDirectories(m_storage.getLibraryPath());
    }
    ////////////////////////////////////////////////////////////





    



    ////////////////////////////////////////////////////////////
    // Classes and functions to keep track of music in library
    ////////////////////////////////////////////////////////////
    private class AlbumInfo {
	String name;
	public int firstSong;
	public int numSongs;
	public int lastSong() {
	    return firstSong + numSongs - 1;
	}
    }

    private AlbumInfo constructAlbumInfo(String band, String album, int startNum) {
	Vector<String> allSongs = getAlbumSongs(band, album);

	AlbumInfo info = new AlbumInfo();
	info.firstSong = startNum;
	info.numSongs = allSongs.size();
	info.name = album;

	return info;
    }


    
    private class BandInfo {
	String name;
	public int firstSong;
	public int numSongs;
	public Vector<AlbumInfo> albums;
	public int lastSong() {
	    return firstSong + numSongs - 1;
	}
    }




    private BandInfo constructBandInfo(String band, int startNum) {
	Vector<String> looseSongs = getLooseSongs(band);
	Vector<String> albums = getAlbums(band);

	BandInfo info = new BandInfo();
	info.name = band;
	info.firstSong = startNum;
	info.albums = new Vector<AlbumInfo>();

	int albumStartNum = startNum + looseSongs.size();
	int albumSongCount = 0;
	for (String album : albums) {
	    AlbumInfo albumInfo = constructAlbumInfo(band, album, albumStartNum);
	    info.albums.add(albumInfo);
	    albumSongCount += albumInfo.numSongs;
	    albumStartNum += albumInfo.numSongs;
	}
	info.numSongs = albumSongCount + looseSongs.size();
	
	return info;
    }
    ////////////////////////////////////////////////////////////















    
    public void constructLibrary()
    {
	int nextSongNum = 0;
	m_allBands = new Vector<BandInfo>();
	m_indexToBand = new TreeMap<Integer, Integer>();

	int i = 0;
	for (String band : getAllBands()) {
	    BandInfo info = constructBandInfo(band, nextSongNum);
	    nextSongNum += info.numSongs;
	    m_allBands.add(info);
	    m_indexToBand.put((Integer)(info.firstSong), i);
	    ++i;
	}
	m_numSongs = nextSongNum;
	advanceToSpecificBandSong(m_allBands.elementAt(0), 0);
    }







    /////////////////////////////////////////////////////////////////////
    // Functions that handle moving to the "next" song.
    // There's a few levels, with the more general ones redirecting to the
    // more specific ones, depending on the situation
    /////////////////////////////////////////////////////////////////////
    private void advanceToSpecificAlbumSong(BandInfo bi, AlbumInfo ai, int songNum) {
	assert (ai != null);
	assert ( (songNum >= ai.firstSong) &&
		 (songNum <= ai.lastSong()));

	Vector<String> albumSongs = getAlbumSongs(bi.name, ai.name);	
	m_currentSongNumber = songNum;
	m_currentSong = albumSongs.elementAt(songNum - ai.firstSong);	
	m_currentAlbum = ai;
	m_currentBand = bi;
    }

    private void advanceToNextAlbumSong() {
	AlbumInfo ai = m_currentAlbum;
	assert (ai != null);

	int songNum = m_currentSongNumber;
	++songNum;
	if (songNum > ai.lastSong()) {
	    songNum = ai.firstSong;
	}

	advanceToSpecificAlbumSong(m_currentBand, ai, songNum);
    }

    private void advanceToSpecificLooseSong(BandInfo bi, int songNum) 
    {
	int index = songNum - bi.firstSong;
	assert index >= 0;
	Vector<String> looseSongs = getLooseSongs(bi.name);
	assert index < looseSongs.size();
	m_currentSongNumber = songNum;
	m_currentSong = looseSongs.elementAt(index);
	m_currentBand = bi;
	m_currentAlbum = null;

    }

    private void advanceToSpecificBandSong(BandInfo bi, int songNum) {
	assert bi != null;
	assert songNum >= bi.firstSong;
	assert songNum <= bi.lastSong();

	Vector<AlbumInfo> albums = bi.albums;
	if (albums.size() == 0 || songNum < albums.elementAt(0).firstSong) {
	    advanceToSpecificLooseSong(bi, songNum);
	} else {
	    // Might be slightly better to do a binary search here
	    // (or maybe not... there will be a lot of cases with a
	    //  small number of albums)
	    int i = 0;
	    while(songNum > albums.elementAt(i).lastSong()) {
		++i;
	    }
	    advanceToSpecificAlbumSong(bi, albums.elementAt(i), songNum);
	}	    	    
    }

    private void advanceToNextBandSong() {
	BandInfo bi = m_currentBand;
	assert bi != null;
	assert m_currentSongNumber >= bi.firstSong;

	int songNum = m_currentSongNumber+1;
	if (songNum > bi.lastSong()) {
	    songNum = bi.firstSong;
	}

	advanceToSpecificBandSong(bi, songNum);
    }

    private void advanceToSpecificSong(int songNum) {
	assert songNum >= 0;
	assert songNum < m_numSongs;

	SortedMap<Integer, Integer> headMap = m_indexToBand.headMap(songNum);
	SortedMap<Integer, Integer> tailMap = m_indexToBand.tailMap(songNum);

	int index;

	// tailmap begins AFTER or AT our desired song.
	// If it begins AT our desired song...
	if (tailMap.size() != 0 && tailMap.firstKey() == songNum) {
	    // ... then our band is the first one in the tail map...
	    index = tailMap.get(tailMap.firstKey());
	} else {
	    // ... otherwise, our band is the last on the head map
	    index = headMap.get(headMap.lastKey());
	}
	
	BandInfo bi = m_allBands.elementAt(index);
	advanceToSpecificBandSong(bi, songNum);
    }

    private void advanceToNextLinearSong() {
	if (m_albumClamp != null && m_albumClamp != "") {
	    // Might wrap to beginning of album
	    advanceToNextAlbumSong();
	} else if (m_currentAlbum != null && m_currentSongNumber < m_currentAlbum.lastSong()) {
	    advanceToNextAlbumSong();
	} else if (m_bandClamp != "") {
	    // Might wrap to beginning of band
	    advanceToNextBandSong();
	} else if (m_currentSongNumber < m_currentBand.lastSong()) {
	    advanceToNextBandSong();
	} else {
	    int targetSong = m_currentSongNumber + 1;
	    assert targetSong <= m_numSongs;
	    if (targetSong == m_numSongs) {
		targetSong = 0;
	    }
	    advanceToSpecificSong(targetSong);
	}

    }

    Random m_random = new Random();
    private void advanceToRandomSong() {
        int firstValid = 0;
        int numValid = m_numSongs;
        if (m_albumClamp != null && m_albumClamp != "") {
            AlbumInfo ai = m_currentAlbum;
            assert (ai.name.equals(m_albumClamp));  // implement random clamping later
            firstValid = ai.firstSong;
            numValid = ai.numSongs;
        } else if (m_bandClamp != "") {
            BandInfo bi = m_currentBand;
            assert (bi.name.equals(m_bandClamp));  // implement random clamping later
            firstValid = bi.firstSong;
            numValid = bi.numSongs;
        }

	advanceToSpecificSong(firstValid + m_random.nextInt(numValid));
    }

    /////////////////////////////////////////////////////////////////////


}

