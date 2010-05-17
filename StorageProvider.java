
import java.util.Vector;

public interface StorageProvider
{
    public String getLibraryPath();
    public String getBandPath(String band);
    public String getAlbumPath(String band, String album);
    public Vector<String> getFilesOrDirs(String path, boolean getFiles);
}