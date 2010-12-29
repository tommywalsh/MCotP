/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */


package com.github.tommywalsh.mcotp;

// this stuff can be removed once we get rid of the debug kill & notify features
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Process;
import android.app.PendingIntent;
import android.widget.Toast;

import android.app.Service;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import android.content.Context;

import android.util.Log;


/// This class is the main interface to/from the rest of the android world
/// This is the code that all UIs and Widgets talk to
/// This is a service which delegates much of its work to code that is not 
/// Android-specific
///
/// It has the following main responsibilites:
///   1) Set up and manage the lifetimes of "SongProvider" and "Player"
///      objects (which are specific to Android) and an "Engine" object
///      (which knows nothing about Android).
///   2) Connect Engine to its SongProvider and Player.
///   3) When Engine/SongProvider change state, send notifications to any UIs 
///       that have registered an interest
///   4) Receive commands sent from UIs, and forward them to Engine or
///       SongProvider


/// In addition, there are some throwaway "features" that help with development 
/// and debugging.  These are pop-up notifications about connections, and the
/// ability to manually kill this process.  These can be removed after this 
/// class has stabilized

public class Backend extends Service {

    AndroidSongProvider m_songProvider;
    Engine m_engine;
    Song m_song;


    ///////////////////// SETUP CODE ////////////////////////////////

    @Override
    public void onStart(Intent intent, int startId) {
	setForeground(true);
    }

    // uncomment for API-5 and higher (1.5 is API-3)
    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
	startForeground(R.string.remote_service_started, new Notification(R.drawable.stat_sample, getText(R.string.remote_service_started),System.currentTimeMillis()));
	return START_STICKY;
	}*/
    // Here we need to get the generic (that is, not Android-specific) 
    // Engine set up and give it an Android-specific SongProvider
    private void initializeGenericComponents() {

	// set up the objects
	Player player = new AndroidPlayer();
	m_songProvider = loadProvider();
	m_engine = new Engine(m_songProvider, player);
	
	// register for updates so we can relay them to UIs
	m_engine.addListener(new Engine.UpdateListener() {
		public void onSongChanged(Song newSong) {
		    notifyChange(true, false);
		}
	    });
    }


    // Loading the song provider gets broken out into its own function.
    // In future, we want to cache the provider's state, rather than create
    // a new one from scratch
    private AndroidSongProvider loadProvider() {
	return new AndroidSongProvider(getContentResolver());
    }
    /*
    private void saveProvider(AndroidSongProvider sp) {
	
	try {
	    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(sp);
	    fos.close();
	} catch (java.io.FileNotFoundException e) {
	} catch (java.io.IOException e) {
	}	
	}*/

    @Override
    public void onCreate() {
	initializeGenericComponents();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();

	//	saveProvider(m_songProvider);

        mCallbacks.kill();
    }

    ///////////////////// END SETUP CODE ////////////////////////////////





    //////////////////// UI COMMUNICATION CODE ////////////////////

    // Allow UIs to bind to our published interfaces
    @Override
    public IBinder onBind(Intent intent) {
        if (IEngine.class.getName().equals(intent.getAction())) {
            return mBinder;
        }
        if (IProvider.class.getName().equals(intent.getAction())) {
            return mSecondaryBinder;
        }
        return null;
    }


    //// CODE TO NOTIFY UIs OF INTERESTING THINGS ////


    // List of registered callbacks
    final RemoteCallbackList<IStatusCallback> mCallbacks
            = new RemoteCallbackList<IStatusCallback>();

    private void notifyChange(boolean engine, boolean provider) {
	final int N = mCallbacks.beginBroadcast();
	for (int i=0; i<N; i++) {
	    try {
		if (engine) {
		    m_song = m_engine.getSong();
		    mCallbacks.getBroadcastItem(i).engineChanged(m_engine.isPlaying(),
								 m_song.bandName(),
								 m_song.albumName(),
								 m_song.songName());
		}
		if (provider) {
		    mCallbacks.getBroadcastItem(i).providerChanged(m_songProvider.isRandom(),
								   m_songProvider.isBandClamped(),
								   m_songProvider.isAlbumClamped());
		}
	    } catch (RemoteException e) {
		// The RemoteCallbackList will take care of removing
		// the dead object for us.
	    }
	}
	mCallbacks.finishBroadcast();
    }
    

    




    //// CODE TO ACCEPT COMMANDS FROM UIs ////

    private final IEngine.Stub mBinder = new IEngine.Stub() {

	    // should be moved into a read-only interface
	    public void registerCallback(IStatusCallback cb) {
		if (cb != null) {
		    mCallbacks.register(cb);
		    notifyChange(true, true);
		}
	    }
	    public void unregisterCallback(IStatusCallback cb) {
		if (cb != null) mCallbacks.unregister(cb);
	    }


	    // Relay each of these commands to our handler, so they 
	    // get run on the correct thread
	    public void togglePlayPause() {
		mHandler.sendEmptyMessage(TOGGLE_PLAY_PAUSE_MSG);
	    }
	    public void repeatCurrentTrack() {
		mHandler.sendEmptyMessage(REPEAT_TRACK_MSG);
	    }
	    public void skipToNextTrack() {
		mHandler.sendEmptyMessage(NEXT_TRACK_MSG);
	    }

	};

    private final IProvider.Stub mSecondaryBinder = new IProvider.Stub() {

	    // Relay each of these commands to our handler, so they 
	    // get run on the correct thread
	    public void toggleBandLocking() {
		mHandler.sendEmptyMessage(TOGGLE_BAND_LOCKING_MSG);
	    }
	    public void toggleAlbumLocking() {
		mHandler.sendEmptyMessage(TOGGLE_ALBUM_LOCKING_MSG);
	    }
	    public void toggleShuffling() {
		mHandler.sendEmptyMessage(TOGGLE_SHUFFLING_MSG);
	    }

    };

    private static final int NEXT_TRACK_MSG = 1;
    private static final int REPEAT_TRACK_MSG = 2;
    private static final int TOGGLE_PLAY_PAUSE_MSG = 3;

    private static final int TOGGLE_BAND_LOCKING_MSG = 10;
    private static final int TOGGLE_ALBUM_LOCKING_MSG = 11;
    private static final int TOGGLE_SHUFFLING_MSG = 12;


    // This code runs on our thread, and will relay UI-initiated commands to 
    // the generic Engine/SongProvider objects
    private final Handler mHandler = new Handler() {
	    @Override public void handleMessage(Message msg) {
		switch (msg.what) {
		    
		case NEXT_TRACK_MSG:
		    m_engine.nextSong();
		    notifyChange(true, false);
		    break;
		case REPEAT_TRACK_MSG:
		    m_engine.restartSong();
		    break;
		case TOGGLE_PLAY_PAUSE_MSG:
		    m_engine.togglePlayPause();
		    notifyChange(true, false);
		    break;
		case TOGGLE_BAND_LOCKING_MSG:
		    m_songProvider.toggleBandClamp();
		    notifyChange(false, true);
		    break;
		case TOGGLE_ALBUM_LOCKING_MSG:
		    m_songProvider.toggleAlbumClamp();
		    notifyChange(false, true);
		    break;
		case TOGGLE_SHUFFLING_MSG:
		    m_songProvider.toggleRandom();
		    notifyChange(false, true);
		    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };


}
