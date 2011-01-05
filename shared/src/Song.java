package com.github.tommywalsh.mcotp;

// Simple interface providing URI
// and some simple display strings.
// Implementations are free to derive or cache whatever
// pieces of this interface make sense for the platform
public interface Song
{
    // This URI will be formatted in a platform-specific manner
    // The platform's implementation of "Player" will need to know
    // what to do with it
    public String uri();

    public String bandName();
    public String albumName();
    public String songName();
}
