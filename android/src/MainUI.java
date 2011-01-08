/*
 * Copyright 2010 Tom Walsh 
 * Redistribution covered by version 3.0 of the GNU General Public Licence available here:
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Based on work by The Android Open Source Project, which was licenced under 
 * version 2.0 of The Apache License
 */

package com.github.tommywalsh.mcotp;

// can be removed when we get rid of kill/notify features
import android.os.Process;
import android.widget.Toast;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import android.util.Log;

public class MainUI extends Activity {
    
    // These are the two main objects we'll be talking to
    IEngine m_engine = null;
    IProvider m_provider = null;

    // On-screen widgets
    Button m_toggleButton;
    Button m_nextButton;
    Button m_repeatButton;
    Button m_shuffleButton;
    ImageButton m_bandLockButton;
    ImageButton m_albumLockButton;
    TextView m_trackText;    
    Button m_albumButton;
    Button m_bandButton;


    // This should be reworked.  Helper classes here are ugly.
    class EngineInfo {
	public boolean isPlaying;
	public String album;
	public String band;
	public String track;
    }

    class ProviderInfo {
	public boolean shuffling;
	public boolean bandLocked;
	public boolean albumLocked;
    }


    // For simplicity's sake, we'll just have a few discrete modes
    // at least for now
    enum Mode {
        ALL_RANDOM,
        BAND_RANDOM,
        BAND_SEQUENTIAL,
        ALBUM_SEQUENTIAL
    }
    Mode m_currentMode;


    private void setModeBooleans(boolean shuffle, boolean bandLock, boolean albumLock) {
	try {
	    m_provider.setMode(shuffle, bandLock, albumLock);
	} catch (android.os.RemoteException e) {
	    // Nothing special required on fail.
	    // UI will update appropriately based on 
	    // return message from service anyhow.
	}
    }

    private void enterAllRandomMode() {
	setModeBooleans(true, false, false);
        m_shuffleButton.setVisibility(android.view.View.INVISIBLE);
        m_currentMode = Mode.ALL_RANDOM;
    }

    private void enterBandRandomMode() {
	setModeBooleans(true, true, false);
        m_shuffleButton.setVisibility(android.view.View.VISIBLE);
        m_currentMode = Mode.BAND_RANDOM;
    }

    private void enterBandSequentialMode() {
	setModeBooleans(false, true, false);
        m_shuffleButton.setVisibility(android.view.View.VISIBLE);
        m_currentMode = Mode.BAND_SEQUENTIAL;
    }

    private void enterAlbumSequentialMode() {
	setModeBooleans(false, true, true);
        m_shuffleButton.setVisibility(android.view.View.INVISIBLE);
        m_currentMode = Mode.ALBUM_SEQUENTIAL;
    }

    


    private void setButtonsEnabled(boolean enabled) {
	m_toggleButton.setEnabled(enabled);
	m_nextButton.setEnabled(enabled);
	m_repeatButton.setEnabled(enabled);
	m_shuffleButton.setEnabled(enabled);
	m_bandLockButton.setEnabled(enabled);
	m_albumLockButton.setEnabled(enabled);
	m_bandButton.setEnabled(enabled);
	m_albumButton.setEnabled(enabled);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	
	// Find and store the buttons, and set up their callbacks
	m_toggleButton = (Button)findViewById(R.id.playPauseButton);
	m_toggleButton.setOnClickListener(m_toggleListener);

	m_nextButton = (Button)findViewById(R.id.skipButton);
	m_nextButton.setOnClickListener(m_nextListener);

	m_repeatButton = (Button)findViewById(R.id.prevButton);
	m_repeatButton.setOnClickListener(m_repeatListener);

	m_bandButton = (Button)findViewById(R.id.bandNameButton);
	m_albumButton = (Button)findViewById(R.id.albumNameButton);

	m_trackText = (TextView)findViewById(R.id.songText);

	m_shuffleButton = (Button)findViewById(R.id.shuffleButton);
	m_shuffleButton.setOnClickListener(m_shuffleListener);

	m_bandLockButton = (ImageButton)findViewById(R.id.bandLockButton);
	m_bandLockButton.setOnClickListener(m_bandLockListener);

	m_albumLockButton = (ImageButton)findViewById(R.id.albumLockButton);
	m_albumLockButton.setOnClickListener(m_albumLockListener);

	setButtonsEnabled(false);

	bindService(new Intent(IEngine.class.getName()),
                    m_engineConnection, Context.BIND_AUTO_CREATE);
	bindService(new Intent(IProvider.class.getName()),
                    m_providerConnection, Context.BIND_AUTO_CREATE);

    }

    private void initPlayer() {
	enterAllRandomMode();
	try {
	    setButtonsEnabled(true);
	    m_engine.skipToNextTrack();	
	} catch (RemoteException e) {
	}
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
	unbindService(m_engineConnection);
	unbindService(m_providerConnection);
    }



    protected void onStart() {
	super.onStart();
    }

    private ServiceConnection m_engineConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            m_engine = IEngine.Stub.asInterface(service);

	    if (m_provider != null) {
		// enable UI only after all interfaces connected!
		initPlayer();
	    }

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                m_engine.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }            
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            m_engine = null;
	    setButtonsEnabled(false);
        }
    };


    private ServiceConnection m_providerConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            m_provider = IProvider.Stub.asInterface(service);
	    if (m_engine != null) {
		// enable UI only after all interfaces connected!
		initPlayer();
	    }
        }

        public void onServiceDisconnected(ComponentName className) {
            m_provider = null;
	    setButtonsEnabled(false);
        }
    };








    ////////////////// BUTTON CALLBACKS //////////////////////////////
    
    private OnClickListener m_toggleListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_engine != null) {
		    try {
			m_engine.togglePlayPause();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_nextListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_engine != null) {
		    try {
			m_engine.skipToNextTrack();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_repeatListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_engine != null) {
		    try {
			m_engine.repeatCurrentTrack();
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_shuffleListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_provider != null) {
		    try {
			if (m_currentMode == Mode.BAND_RANDOM) {
			    enterBandSequentialMode();
			} else if (m_currentMode ==  Mode.BAND_SEQUENTIAL) {
			    enterBandRandomMode();
			} else {
			    m_provider.toggleShuffling();
			}
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};

    private OnClickListener m_bandLockListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_provider != null) {
		    try {
			if (m_currentMode == Mode.ALL_RANDOM) {
			    enterBandRandomMode();
			} else if (m_currentMode == Mode.BAND_RANDOM || m_currentMode == Mode.BAND_SEQUENTIAL) {
			    enterAllRandomMode();
			} else if (m_currentMode == Mode.ALBUM_SEQUENTIAL) {
			    enterBandRandomMode();
			} else {
			    m_provider.toggleBandLocking();
			}
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};
    private OnClickListener m_albumLockListener = new OnClickListener() {
	    public void onClick(View v) {
		if (m_provider != null) {
		    try {
			switch (m_currentMode) {
			case ALBUM_SEQUENTIAL:
			    enterAllRandomMode();
			    break;
			case BAND_SEQUENTIAL:
			case BAND_RANDOM:
			case ALL_RANDOM:
			    enterAlbumSequentialMode();
			    break;
			default:
			    m_provider.toggleAlbumLocking();
			}
		    } catch (RemoteException ex) {
			// server process died.. will clean up if necessary in disconnect code
		    }
		}
	    }
	};

    /////////////////// END BUTTON CALLBACKS //////////////////////









    // Updates from the backend
    private IStatusCallback mCallback = new IStatusCallback.Stub() {
	    // Here we need to relay the information to a helper running on our own thread

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
		ProviderInfo pi = new ProviderInfo();
		pi.shuffling = shuffle;
		pi.bandLocked = bandLock;
		pi.albumLocked = albumLock;
		
		mHandler.sendMessage(mHandler.obtainMessage(PROVIDER_UPDATE, pi));
	    }

    };
    
    private static final int PROVIDER_UPDATE = 1;
    private static final int ENGINE_UPDATE = 2;


    private void onProviderUpdate (ProviderInfo pi)
    {
	// We've been called before, so from now on, just update the UI
	m_shuffleButton.setText(pi.shuffling ?
				R.string.sequential :
				R.string.random);
	m_bandLockButton.setImageResource(pi.bandLocked ? 
					  R.drawable.locked :
					  R.drawable.unlocked);
	m_albumLockButton.setImageResource(pi.albumLocked ?
					   R.drawable.locked :
					   R.drawable.unlocked);
    }

    // Here's the handler that updates the UI
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
	    case ENGINE_UPDATE:
		EngineInfo ei = (EngineInfo)(msg.obj);
		m_toggleButton.setText( ei.isPlaying ?
					R.string.pause :
					R.string.play );
		m_bandButton.setText(ei.band);
		m_albumButton.setText(ei.album);
		m_trackText.setText(ei.track);
		break;
	    case PROVIDER_UPDATE: 
		ProviderInfo pi = (ProviderInfo)(msg.obj);
		onProviderUpdate(pi);
		break;
	    default:
		super.handleMessage(msg);
            }
        }
        
    };
}


