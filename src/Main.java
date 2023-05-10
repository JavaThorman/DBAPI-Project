import src.KeyReader;
import src.SpotifySearch;

public class Main {
    public static void main(String[] args) {

        KeyReader keyReader = new KeyReader("RapidAPI");
        SpotifySearch spotifySearch = new SpotifySearch();
        spotifySearch.searchArtist("Tupac");

    }
}