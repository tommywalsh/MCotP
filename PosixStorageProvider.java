
import java.io.File;
import java.util.Vector;
import java.util.Arrays;

public class PosixStorageProvider implements StorageProvider
{
    private String m_root;

    public PosixStorageProvider(String root) {
	m_root = root;
    }

    public String getLibraryPath() {
	return m_root;
    }

    public String getBandPath(String band) {
	return m_root + "/" + band;
    }
    
    public String getAlbumPath(String band, String album) {
	return getBandPath(band) + "/" + album;
    }

    private String getSongPath(String band, String album, String song) {
	String sp = getBandPath(band);
	if (album != null) {
	    sp += "/" + album;
	}
	sp += "/" + song;
	return sp;
    }

    public String getSongPath(Song song) {
	return getSongPath(song.bandName(), song.albumName(), song.songName());
    }

    public Vector<String> getFilesOrDirs(String path, boolean getFiles)
    {
	Vector<String> matches = new Vector<String>();
	File dir = new File(path);
	String[] entries = dir.list();
	if (entries.length != 0) {
	    Arrays.sort(entries);
	    
	    
	    for (String entry : entries) {
		File file = new File(path + "/" + entry);
		if (getFiles) {
		    if (file.isFile()) {
			matches.add(file.getName());
		    }
		} else if (file.isDirectory()) {
		    matches.add(file.getName());
		}
	    }
	}
	    return matches;
    }
}
