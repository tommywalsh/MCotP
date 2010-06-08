package com.github.tommywalsh.mcotp.android;

import com.github.tommywalsh.mcotp.shared.*;

import android.app.Activity;
import android.os.Bundle;
import java.util.Vector;
import android.widget.TextView;


public class AndroidUI extends Activity
{
    private void init() {
	PosixStorageProvider psp = new PosixStorageProvider("/sdcard/music");
	SongProvider sp = new SongProvider(psp);
	sp.constructLibrary();
	AndroidPlayer pl = new AndroidPlayer(psp);
	final Engine engine = new Engine(sp, pl);

    }


    private TextView m_songText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	init();
	m_songText = (TextView) findViewById(R.id.songText);

	PosixStorageProvider psp = new PosixStorageProvider("/sdcard/music");
	Vector<String> vs = psp.getFilesOrDirs(psp.getLibraryPath(), false);
	Integer s = vs.size();
	m_songText.setText("Number of bands: " + s.toString());
    }
}
