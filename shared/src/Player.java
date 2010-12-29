package com.github.tommywalsh.mcotp;


// This interface will need to be implemented specially for
// each substatially different platform that MCotP will run 
// on.  This is the interface used by the engine to control
// music playback.  Implementations will need to hook these
// methods up however necessary to play audio on the platform

public interface Player
{

    public interface SongFinishedListener
    {
	public void onSongFinished();
    }

    public void addListener(SongFinishedListener l);
    public void removeListener(SongFinishedListener l);
    public void play();
    public void pause();
    public void restartSong();
    public void setSong(Song song);
}
