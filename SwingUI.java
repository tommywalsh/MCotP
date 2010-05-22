import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class SwingUI extends JPanel implements ActionListener
{
    Engine m_engine;
    
    JButton m_shuffleButton;
    
    JButton m_genreButton;
    JButton m_genreLockButton;

    JButton m_bandButton;
    JButton m_bandLockButton;

    JButton m_albumButton;
    JButton m_albumLockButton;

    JLabel  m_songLabel;

    JButton m_restartButton;
    JButton m_playButton;
    JButton m_skipButton;

    private void addRow(JComponent a, JComponent b, JComponent c) {
	JPanel subPanel = new JPanel();
	subPanel.add(a);
	if (b != null) {
	    subPanel.add(b);
	    if (c != null) {
		subPanel.add(c);
	    }
	}
	add(subPanel);
    }

    public SwingUI(JFrame frame, Engine engine) {
	m_engine = engine;

	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

	m_shuffleButton = new JButton("Shuffle Mode");
	m_shuffleButton.setActionCommand("shuffle");
	m_shuffleButton.addActionListener(this);
	add(m_shuffleButton);

	m_genreButton     = new JButton("All genres");
	m_genreLockButton = new JButton("Lock");
	addRow(m_genreButton, m_genreLockButton, null);

	m_bandButton = new JButton("Band");
	m_bandLockButton = new JButton("Lock");
	addRow(m_bandButton, m_bandLockButton, null);

	m_albumButton = new JButton("Album");
	m_albumLockButton = new JButton("Lock");
	addRow(m_albumButton, m_albumLockButton, null);

	m_songLabel = new JLabel("Song");
	add(m_songLabel);

	m_playButton = new JButton("Play/Pause");
	m_playButton.setActionCommand("play");
	m_playButton.addActionListener(this);

	m_skipButton = new JButton("Skip");
	m_skipButton.setActionCommand("skip");
	m_skipButton.addActionListener(this);

	m_restartButton = new JButton("Back");
	m_restartButton.setActionCommand("back");
	m_restartButton.addActionListener(this);
	addRow(m_restartButton, m_playButton, m_skipButton);

	m_engine.addListener(new Engine.UpdateListener() {
		public void onSongChanged(Song newSong) {
		    m_bandButton.setText(newSong.bandName());

		    String an = newSong.albumName();
		    if (an == null) {
			m_albumButton.setText("No Album");
		    } else {
			m_albumButton.setText(an);
		    }
		    m_albumButton.setEnabled(an != null);
		    m_albumLockButton.setEnabled(an != null);

		    m_songLabel.setText(newSong.songName());
		}
	    });

    }

    public void actionPerformed(ActionEvent e) {
	String ac = e.getActionCommand();
	if (ac.equals("shuffle")) {
	    m_engine.toggleRandom();
	} else if (ac.equals("play")) {
	    m_engine.togglePlayPause();
	} else if (ac.equals("skip")) {
	    m_engine.nextSong();
	} else if (ac.equals("back")) {
	    m_engine.restartSong();
	}
    }


    private static void runGUIApp(String path) {

	PosixStorageProvider psp = new PosixStorageProvider(path);
	SongProvider sp = new SongProvider(psp);
	sp.constructLibrary();
	SoxPlayer pl = new SoxPlayer(psp);
	final Engine engine = new Engine(sp, pl);

	JFrame frame = new JFrame("MCotP");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	SwingUI content = new SwingUI(frame, engine);
	content.setOpaque(true);
	frame.setContentPane(content);

	frame.pack();
	frame.setVisible(true);
    }

    public static void main(String[] args) {
	
	if (args.length != 1) {
	    System.out.println("Usage: java Engine /path/to/library");
	} else {
	    final String path = args[0];
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			runGUIApp(path);
		    }
		});
	}
    }
}
