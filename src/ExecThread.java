import java.lang.Process;
import java.lang.Runtime;
import java.io.IOException;
import java.lang.Thread;

// Class that runs a (system) command in another thread
// The external process will be terminated if the thread is interrupted
public class ExecThread extends Thread {
    private String[] m_cmdAndArgs;
    private boolean m_isRunning = false;

    public interface FinishedListener {
	public void onFinished();
    }
    private FinishedListener m_listener;


    // First item in array is the command to run, remainder are args to the command
    // Listener will be called if process finishes normally (i.e. not interrupted)
    public ExecThread(String[] cmdAndArgs, FinishedListener l) {
	m_cmdAndArgs = cmdAndArgs;
	m_listener = l;
    }    

    public synchronized boolean isRunning() {
	return m_isRunning;
    }

    
    // Don't call this.  Call "start" instead.
    public void run() {
	Runtime runtime = Runtime.getRuntime();
	m_isRunning = true;
	boolean finished = false;
	try {
	    // Run the command (asynchronously)...
	    Process proc = runtime.exec(m_cmdAndArgs);
	    try {
		// ...but wait for it to finish...
		proc.waitFor();
		finished = true;
	    } catch (InterruptedException e) {
		// ...unless we are interrupted, in which case
		// we kill the external process
		proc.destroy();
	    }
	} catch (IOException e) {
	}
	m_isRunning = false;
	if (finished) {
	    m_listener.onFinished();
	}
    }
}
