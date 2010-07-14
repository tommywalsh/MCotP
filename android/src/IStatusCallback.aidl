/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */

package com.github.tommywalsh.mcotp;

// updates from the backend (e.g. to update UI)
oneway interface IStatusCallback {
    void engineChanged(boolean isPlaying, String bandName, String albumName, String trackName);
    void providerChanged(boolean isShuffling, boolean isBandLocked, boolean isAlbumLocked);
}
