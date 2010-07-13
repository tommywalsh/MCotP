/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */

package com.github.tommywalsh.mcotp;

// Interface to the music provider (lock on band, random play, etc)
interface IProvider {
    int getPid();

    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
