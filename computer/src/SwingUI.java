package com.github.tommywalsh.mcotp;


import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

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
        m_bandLockButton.setActionCommand("lock band");
        m_bandLockButton.addActionListener(this);
	addRow(m_bandButton, m_bandLockButton, null);

	m_albumButton = new JButton("Album");
	m_albumLockButton = new JButton("Lock");
        m_albumLockButton.setActionCommand("lock album");
        m_albumLockButton.addActionListener(this);
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
	} else if (ac.equals("lock band")) {
	    m_engine.toggleBandClamp();
            if (m_bandLockButton.getText().equals("Unlock")) {
                m_bandLockButton.setText("Lock");
            } else {                
                m_bandLockButton.setText("Unlock");
                m_albumLockButton.setText("Lock");
            }
        } else if (ac.equals("lock album")) {
	    m_engine.toggleAlbumClamp();
            if (m_albumLockButton.getText().equals("Unlock")) {
                m_albumLockButton.setText("Lock");
            } else {                
                m_bandLockButton.setText("Lock");
                m_albumLockButton.setText("Unlock");
            }
        }
    }


    private static void runGUIApp(String path) {

	PosixStorageProvider psp = new PosixStorageProvider(path);
	StorageSongProvider sp = null;

	File f = new File("/home/tom/.mcotp");
	try {
	    if (f.canRead()) {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		sp = (StorageSongProvider)(ois.readObject());
		sp.initAfterDeserialization(psp);
	    }

        // No need to do anything about these exceptions,
        // just construct the library from scratch
	} catch (java.io.FileNotFoundException e) {
	} catch (java.io.IOException e) {
	} catch (java.lang.ClassNotFoundException e) {
	}

	if (sp == null) {
	    sp = new StorageSongProvider(psp);
	    sp.constructLibrary();
	    try {
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(sp);
	    } catch (java.io.FileNotFoundException e) {
	    } catch (java.io.IOException e) {
	    }
	}
	

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
