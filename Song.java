public class Song
{
    private String m_band;
    private String m_album;
    private String m_songFile;

    public Song(String band, String album, String songFile) {
        m_band = band;
        m_album = album;
        m_songFile = songFile;
    }

    public String bandName() {
        return m_band;
    }

    public String albumName() {
        return m_album;
    }

    public String songName() {
        return m_songFile;
    }
}
