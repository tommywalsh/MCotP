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
		if (cb != null) mCallbacks.register(cb);
	    }
	    public void unregisterCallback(IStatusCallback cb) {
		if (cb != null) mCallbacks.unregister(cb);
	    }
	    public void togglePlayPause() {
		mHandler.sendEmptyMessage(TOGGLE_PLAY_PAUSE_MSG);
	    }

	};

    private final IProvider.Stub mSecondaryBinder = new IProvider.Stub() {
        public int getPid() {
            return Process.myPid();
        }

        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString) {
        }
    };

    
    private static final int TOGGLE_PLAY_PAUSE_MSG = 2;

    /**
     * Our Handler used to execute operations on the main thread.  This is used
     * to schedule increments of our value.
     */
    private final Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {

	    case TOGGLE_PLAY_PAUSE_MSG:
		{
		    m_isPlaying = !m_isPlaying;
		    final int N = mCallbacks.beginBroadcast();
		    for (int i=0; i<N; i++) {
			try {
			    mCallbacks.getBroadcastItem(i).playModeChanged(m_isPlaying);
			} catch (RemoteException e) {
			    // The RemoteCallbackList will take care of removing
			    // the dead object for us.
			}
		    }
		    mCallbacks.finishBroadcast();
		    break;
		}

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
