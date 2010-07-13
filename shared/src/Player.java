package com.github.tommywalsh.mcotp.shared;


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
