package mcotp;

import android.app.Activity;
import android.os.Bundle;
import java.util.Vector;
import android.widget.TextView;


public class AndroidUI extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	//        setContentView(R.layout.main);

	PosixStorageProvider psp = new PosixStorageProvider("/sdcard/music");
	Vector<String> vs = psp.getFilesOrDirs(psp.getLibraryPath(), false);
	TextView tv = new TextView(this);
	Integer s = vs.size();
	tv.setText("Number of bands: " + s.toString());
	//	tv.setText(vs.elementAt(s-1));
	setContentView(tv);
    }
}
