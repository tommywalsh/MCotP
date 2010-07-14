/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */


package com.github.tommywalsh.mcotp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.widget.Toast;

import java.util.HashMap;

// Slightly modified copy of the sample RemoteService class
public class Backend extends Service {

    // package scoped for easy access from embedded classes
    final RemoteCallbackList<IStatusCallback> mCallbacks
            = new RemoteCallbackList<IStatusCallback>();
    
    NotificationManager mNM;
    boolean m_isPlaying = false;
    // following should be in a song structure, probably
    String m_bandName = "foo";
    String m_albumName = "bar";
    String m_trackName = "foo bar";
    boolean m_bandLocked = false;
    boolean m_albumLocked = false;
    boolean m_shuffling = false;
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        showNotification();
        
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.remote_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
        
        // Unregister all callbacks.
        mCallbacks.kill();
        
	//        mHandler.removeMessages(REPORT_MSG);
    }
    

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


    private final IEngine.Stub mBinder = new IEngine.Stub() {
	    public void registerCallback(IStatusCallback cb) {
		if (cb != null) {
		    mCallbacks.register(cb);
		    notifyChange(true, true);
		}
	    }
	    public void unregisterCallback(IStatusCallback cb) {
		if (cb != null) mCallbacks.unregister(cb);
	    }
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

	    public void toggleBandLocking() {
		mHandler.sendEmptyMessage(TOGGLE_BAND_LOCKING_MSG);
	    }
	    public void toggleAlbumLocking() {
		mHandler.sendEmptyMessage(TOGGLE_ALBUM_LOCKING_MSG);
	    }
	    public void toggleShuffling() {
		mHandler.sendEmptyMessage(TOGGLE_SHUFFLING_MSG);
	    }

        public int getPid() {
            return Process.myPid();
        }

        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString) {
        }
    };

    private static final int NEXT_TRACK_MSG = 1;
    private static final int REPEAT_TRACK_MSG = 2;
    private static final int TOGGLE_PLAY_PAUSE_MSG = 3;

    private static final int TOGGLE_BAND_LOCKING_MSG = 10;
    private static final int TOGGLE_ALBUM_LOCKING_MSG = 11;
    private static final int TOGGLE_SHUFFLING_MSG = 12;


	    private void notifyChange(boolean engine, boolean provider) {
		final int N = mCallbacks.beginBroadcast();
		for (int i=0; i<N; i++) {
		    try {
			if (engine) {
			    mCallbacks.getBroadcastItem(i).engineChanged(m_isPlaying,
									 m_bandName,
									 m_albumName,
									 m_trackName);
			}
			if (provider) {
			    mCallbacks.getBroadcastItem(i).providerChanged(m_shuffling,
									   m_bandLocked,
									   m_albumLocked);
			}
		    } catch (RemoteException e) {
			// The RemoteCallbackList will take care of removing
			// the dead object for us.
		    }
		}
		mCallbacks.finishBroadcast();
	    }



    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private final Handler mHandler = new Handler() {
	    @Override public void handleMessage(Message msg) {
		switch (msg.what) {
		    
		case NEXT_TRACK_MSG:
		    notifyChange(true, false);
		    break;
		case REPEAT_TRACK_MSG:
		    // nothing to do until engine really hooked up
		    break;
		case TOGGLE_PLAY_PAUSE_MSG:
		    m_isPlaying = !m_isPlaying;
		    notifyChange(true, false);
		    break;
		case TOGGLE_BAND_LOCKING_MSG:
		    m_bandLocked = !m_bandLocked;
		    notifyChange(false, true);
		case TOGGLE_ALBUM_LOCKING_MSG:
		    m_albumLocked = !m_albumLocked;
		    notifyChange(false, true);
		case TOGGLE_SHUFFLING_MSG:
		    m_shuffling = !m_shuffling;
		    notifyChange(false, true);

                default:
                    super.handleMessage(msg);
            }
        }
    };

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.remote_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainUI.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.remote_service_started, notification);
    }
}
