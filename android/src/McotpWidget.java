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
import android.content.ComponentName;

public class McotpWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
	
	context.startService(new Intent(context, UpdateService.class));
	

	/*
	RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.mcotp_widget);
	updateViews.setTextViewText(R.id.widget_band, "Band");
	updateViews.setTextViewText(R.id.widget_song, "Song");
	for (int i=0; i < appWidgetIds.length; i++) {
	    appWidgetManager.updateAppWidget(appWidgetIds[i], updateViews);
	    }*/
    }

    public static class UpdateService extends Service {
        @Override
	    public void onStart(Intent intent, int startId) {
            RemoteViews updateViews = buildUpdate(this);
	    
            ComponentName thisWidget = new ComponentName(this, McotpWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }
	
        public RemoteViews buildUpdate(Context context) {
	    
	    RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_message);
	    updateViews.setTextViewText(R.id.message, "Waiting for MCotP service");
	    return updateViews;
        }
	
        @Override
	public IBinder onBind(Intent intent) {
            return null;
	}
    }
}
