/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */

package com.github.tommywalsh.mcotp;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainUI extends Activity {
    IEngine mEngineService = null;
    IProvider mSecondaryService = null;
    
    Button mKillButton;
    Button m_toggleButton;
    Button m_nextButton;
    Button m_repeatButton;

    TextView mCallbackText;

    private boolean mIsBound;

    /**
     * Standard initialization of this activity.  Set up the UI, then wait
     * for the user to poke it before doing anything.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_service_binding);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button)findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        mKillButton = (Button)findViewById(R.id.kill);
        mKillButton.setOnClickListener(mKillListener);
        mKillButton.setEnabled(false);
        
	m_toggleButton = (Button)findViewById(R.id.toggle);
	m_toggleButton.setOnClickListener(m_toggleListener);
	m_toggleButton.setEnabled(false);

	m_nextButton = (Button)findViewById(R.id.next);
	m_nextButton.setOnClickListener(m_nextListener);
	m_nextButton.setEnabled(false);

	m_repeatButton = (Button)findViewById(R.id.repeat);
	m_repeatButton.setOnClickListener(m_repeatListener);
	m_repeatButton.setEnabled(false);


        mCallbackText = (TextView)findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mEngineService = IEngine.Stub.asInterface(service);
            mKillButton.setEnabled(true);
	    m_toggleButton.setEnabled(true);
	    m_nextButton.setEnabled(true);
	    m_repeatButton.setEnabled(true);
            mCallbackText.setText("Attached.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mEngineService.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            
            // As part of the sample, tell the user what happened.
            Toast.makeText(MainUI.this, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mEngineService = null;
            mKillButton.setEnabled(false);
	    m_toggleButton.setEnabled(false);
	    m_nextButton.setEnabled(false);
	    m_repeatButton.setEnabled(false);

            mCallbackText.setText("Disconnected.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(MainUI.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };


    private ServiceConnection mSecondaryConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // Connecting to a secondary interface is the same as any
            // other interface.
            mSecondaryService = IProvider.Stub.asInterface(service);
            mKillButton.setEnabled(true);
	    m_toggleButton.setEnabled(true);
	    m_nextButton.setEnabled(true);
	    m_repeatButton.setEnabled(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSecondaryService = null;
            mKillButton.setEnabled(false);
	    m_toggleButton.setEnabled(false);
	    m_nextButton.setEnabled(false);
	    m_repeatButton.setEnabled(false);

        }
    };

    private OnClickListener mBindListener = new OnClickListener() {
        public void onClick(View v) {
            // Establish a couple connections with the service, binding
            // by interface names.  This allows other applications to be
            // installed that replace the remote service by implementing
            // the same interface.
            bindService(new Intent(IEngine.class.getName()),
                    mConnection, Context.BIND_AUTO_CREATE);
            bindService(new Intent(IProvider.class.getName()),
                    mSecondaryConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
            mCallbackText.setText("Binding.");
        }
    };

    private OnClickListener mUnbindListener = new OnClickListener() {
        public void onClick(View v) {
            if (mIsBound) {
                // If we have received the service, and hence registered with
                // it, then now is the time to unregister.
                if (mEngineService != null) {
                    try {
                        mEngineService.unregisterCallback(mCallback);
                    } catch (RemoteException e) {
                        // There is nothing special we need to do if the service
                        // has crashed.
                    }
                }
                
                // Detach our existing connection.
                unbindService(mConnection);
                unbindService(mSecondaryConnection);
                mKillButton.setEnabled(false);
		m_toggleButton.setEnabled(false);
		m_nextButton.setEnabled(false);
		m_repeatButton.setEnabled(false);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }
    };

    private OnClickListener m_toggleListener = new OnClickListener() {
	    public void onClick(View v) {
		if (mEngineService != null) {
		    try {
			mEngineService.togglePlayPause();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_nextListener = new OnClickListener() {
	    public void onClick(View v) {
		if (mEngineService != null) {
		    try {
			mEngineService.skipToNextTrack();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_repeatListener = new OnClickListener() {
	    public void onClick(View v) {
		if (mEngineService != null) {
		    try {
			mEngineService.repeatCurrentTrack();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};

    private OnClickListener mKillListener = new OnClickListener() {
        public void onClick(View v) {
            // To kill the process hosting our service, we need to know its
            // PID.  Conveniently our service has a call that will return
            // to us that information.
            if (mSecondaryService != null) {
                try {
                    int pid = mSecondaryService.getPid();
                    // Note that, though this API allows us to request to
                    // kill any process based on its PID, the kernel will
                    // still impose standard restrictions on which PIDs you
                    // are actually able to kill.  Typically this means only
                    // the process running your application and any additional
                    // processes created by that app as shown here; packages
                    // sharing a common UID will also be able to kill each
                    // other's processes.
                    Process.killProcess(pid);
                    mCallbackText.setText("Killed service process.");
                } catch (RemoteException ex) {
                    // Recover gracefully from the process hosting the
                    // server dying.
                    // Just for purposes of the sample, put up a notification.
                    Toast.makeText(MainUI.this,
                            R.string.remote_call_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    
    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------
    
    class EngineInfo {
	public boolean isPlaying;
	public String album;
	public String band;
	public String track;
    }
	    
    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IStatusCallback mCallback = new IStatusCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
	    public void engineChanged(boolean isPlaying, String band, String album, String track) {
		EngineInfo ei = new EngineInfo();
		ei.isPlaying = isPlaying;
		ei.band = band;
		ei.album = album;
		ei.track = track;

		mHandler.sendMessage(mHandler.obtainMessage(ENGINE_UPDATE, ei));
	    }
	    
	    public void providerChanged(boolean shuffle, boolean bandLock, boolean albumLock)
	    {
	    }

    };
    
    private static final int BUMP_MSG = 1;
    private static final int ENGINE_UPDATE = 2;
    
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
	    case ENGINE_UPDATE:
		EngineInfo ei = (EngineInfo)(msg.obj);
		m_toggleButton.setText( ei.isPlaying ?
					R.string.pause :
					R.string.play );
		break;
	    default:
		super.handleMessage(msg);
            }
        }
        
    };
}


