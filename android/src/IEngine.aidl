/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */

package com.github.tommywalsh.mcotp;

import com.github.tommywalsh.mcotp.IStatusCallback;

// Interface to the music playing engine (play/pause/advance/etc.)
interface IEngine {
    void registerCallback(IStatusCallback cb);
    void unregisterCallback(IStatusCallback cb);
}
