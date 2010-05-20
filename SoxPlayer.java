import java.lang.Process;
import java.lang.Runtime;
import java.io.IOException;
import java.lang.Thread;
import java.util.HashSet;
import java.util.Iterator;

public class SoxPlayer implements Player, ExecThread.FinishedListener
{
    private final String c_playCommand = "play";
    private ExecThread m_thread;
    private String m_songFile;
    private HashSet<Player.SongFinishedListener> m_listeners = new HashSet<Player.SongFinishedListener>();

    SoxPlayer() {
    }

    public void onFinished() {
	for (Iterator<Player.SongFinishedListener> iter = m_listeners.iterator(); iter.hasNext();  ) {
	    iter.next().onSongFinished();
	}	
    }

    public void addListener(Player.SongFinishedListener l) {
	m_listeners.add(l);
    }

    public void removeListener(Player.SongFinishedListener l) {
	m_listeners.remove(l);
    }

    public void play() {
	String[] ca = new String[2];
	ca[0] = c_playCommand;
	ca[1] = m_songFile;
	m_thread = new ExecThread(ca, this);
	m_thread.start();
    }

    private void stopCold() {
	if (m_thread != null) {
	    m_thread.interrupt();
	    try {
		m_thread.join();
	    } catch (InterruptedException e) {
	    }
	    m_thread = null;
	}
    }

    public void pause() {
	// pausing not implemented yet.  But, we can stop cold.
	stopCold();
    }

    public void restartSong() {
	stopCold();
	play();
    }


    public void setSongFile(String songFile) {
	stopCold();
	m_songFile = songFile;
    }

    /* For testing 
    public static final void main(String[] args) {
	SoxPlayer sp = new SoxPlayer();

	try {
	    System.out.println("Starting song, then waiting four seconds, then pausing\n");
	    System.out.flush();
	    sp.setSongFile("jmp.mp3");
	    sp.play();
	    Thread.sleep(4000);
	    sp.pause();

	    System.out.println("Starting song, then waiting four seconds, then restarting\n");
	    System.out.flush();
	    sp.play();
	    Thread.sleep(4000);
	    sp.restartSong();

	    System.out.println("Pausing 4 more seconds, then starting another song\n");
	    Thread.sleep(4000);
	    sp.setSongFile("/media/mcotp/ABC/Look of Love.mp3");
	    sp.play();
 	    Thread.sleep(4000);
	    sp.pause();

	} catch (InterruptedException e) {
	    System.out.println("Interrupted\n");
	    System.out.flush();	    
	}
    }
    */
}
