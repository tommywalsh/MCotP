
import java.util.Vector;

public interface StorageProvider
{
    public String getLibraryPath();
    public String getBandPath(String band);
    public String getAlbumPath(String band, String album);

    // Pass null for Album in the case of loose songs
    public String getSongPath(String band, String album, String song);
    public Vector<String> getFilesOrDirs(String path, boolean getFiles);
}