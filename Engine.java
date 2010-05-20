

public class Engine
{
    private SongProvider m_provider;
    private Player m_player;
    private String m_currentSongFile;
    private boolean m_isPlaying;

    Engine(SongProvider provider, Player player) {
	m_provider = provider;
	m_player = player;
	m_isPlaying = false;
	m_currentSongFile = m_provider.getCurrentSongFile();
	m_player.setSongFile(m_currentSongFile);
    }

    public void toggleRandom() {
	m_provider.toggleRandom();
    }

    public void setClamp(String genreClamp, String bandClamp, String albumClamp) {
	if (albumClamp != null) {
	    assert bandClamp != null;
	    m_provider.setAlbumClamp(bandClamp, albumClamp);
	} else if (bandClamp != null) {
	    m_provider.setBandClamp(bandClamp);
	} else {
	    assert genreClamp != null;
	    m_provider.setGenreClamp(genreClamp);
	}
	String songFile = m_provider.getCurrentSongFile();
	if (songFile != m_currentSongFile) {
	    nextSong();
	}
    }

    public void play() {
	if (!m_isPlaying) {
	    m_isPlaying = true;
	    m_player.play();
	}
    }
    public void pause() {
	if (m_isPlaying) {
	    m_isPlaying = false;
	    m_player.pause();
	}
    }

    public void togglePlayPause() {
	if (m_isPlaying) {
	    m_player.pause();
	} else {
	    m_player.play();
	}
    }

    public void restartSong() {
	m_player.restartSong();
    }

    public void nextSong() {
	m_provider.advanceSong();
	String songFile = m_provider.getCurrentSongFile();
	m_player.pause();
	m_player.setSongFile(songFile);
	if (m_isPlaying) {
	    m_player.play();
	}
    }

    // For testing
    public static void main (String[] args)
    {
	if (args.length != 1) {
	    System.out.println("Usage: java Engine /path/to/library");
	} else {
	    SongProvider sp = new SongProvider(new PosixStorageProvider(args[0]));
	    sp.constructLibrary();
	    
	    Engine engine = new Engine(sp, new SoxPlayer());
	    try {
		
		engine.toggleRandom();
		engine.play();
		for (int i = 1; i < 10; ++i) {
		    Thread.sleep(4000);
		    engine.nextSong();
		}
		engine.pause();
		
	    } catch (InterruptedException e) {
	    }
	}
    }

}