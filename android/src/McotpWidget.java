/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommywalsh.mcotp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.ServiceConnection;

public class McotpWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
	
	context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {
        @Override
	    public void onStart(Intent intent, int startId) {
	    updateUI("Waiting for MCotP service");

	    bindService(new Intent(IEngine.class.getName()),
			m_connection, Context.BIND_AUTO_CREATE);
        }

	private void updateUI(String message) {
            RemoteViews updateViews = buildUpdate(this, message);
	    
            ComponentName thisWidget = new ComponentName(this, McotpWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
	}

	private void updateUI(String band, String song) {
            RemoteViews updateViews = buildUpdate(this, band, song);
	    
            ComponentName thisWidget = new ComponentName(this, McotpWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
	}
	
        public RemoteViews buildUpdate(Context context, String message) {
	    
	    RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_message);
	    updateViews.setTextViewText(R.id.message, message);
	    return updateViews;
        }

        public RemoteViews buildUpdate(Context context, String band, String song) {
	    
	    RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.mcotp_widget);
	    updateViews.setTextViewText(R.id.widget_band, band);
	    updateViews.setTextViewText(R.id.widget_song, song);
	    return updateViews;
        }
	
        @Override
	public IBinder onBind(Intent intent) {
            return null;
	}

	IEngine m_engine = null;


	private ServiceConnection m_connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
					       IBinder service) {

		    m_engine = IEngine.Stub.asInterface(service);
		    
		    try {
			m_engine.registerCallback(m_callback);
		    } catch (RemoteException e) {
		    }
		    
		    updateUI("Connected!");
		}

		public void onServiceDisconnected(ComponentName className) {
		    updateUI("Disconnected!");
		}



		private IStatusCallback m_callback = new IStatusCallback.Stub() {
			public void engineChanged(boolean isPlaying, String band, String album, String song) {
			    updateUI(band, song);
			}
	    
			public void providerChanged(boolean shuffle, boolean bandLock, boolean albumLock)
			{
			}

		    };
		
		
	    };

    }
}
