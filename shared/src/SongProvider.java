package com.github.tommywalsh.mcotp;

public interface SongProvider
{
    // The next two functions give an iterator-like interface
    // and allow the engine to easily cycle over all applicable songs
    public Song getCurrentSong();
    public void advanceSong();
    public void toggleRandom();
    public boolean isRandom();
    public void setGenreClamp(String genreClamp);
    public void setBandClamp(String band);
    public void setAlbumClamp(String band, String album);
    public boolean isBandClamped();
    public boolean isAlbumClamped();
}
