import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class SwingUI extends JPanel implements ActionListener
{
    Engine m_engine;
    
    JButton m_shuffleButton;
    JButton m_playButton;
    JButton m_skipButton;
    JLabel  m_songLabel;

    public SwingUI(JFrame frame, Engine engine) {
	m_engine = engine;

	m_shuffleButton = new JButton("Toggle shuffle mode");
	m_shuffleButton.setActionCommand("shuffle");
	m_shuffleButton.addActionListener(this);
	add(m_shuffleButton);

	m_playButton = new JButton("Play/Pause");
	m_playButton.setActionCommand("play");
	m_playButton.addActionListener(this);
	add(m_playButton);

	m_skipButton = new JButton("Skip");
	m_skipButton.setActionCommand("skip");
	m_skipButton.addActionListener(this);
	add(m_skipButton);

	m_songLabel = new JLabel(engine.getSongFile());
	add(m_songLabel);

	m_engine.addListener(new Engine.UpdateListener() {
		public void onSongChanged(String newSong) {
		    m_songLabel.setText(newSong);
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
	}
    }


    private static void runGUIApp(String path) {

	SongProvider sp = new SongProvider(new PosixStorageProvider(path));
	sp.constructLibrary();
	
	final Engine engine = new Engine(sp, new SoxPlayer());

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
