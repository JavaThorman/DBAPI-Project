package src;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Scanner;



public class SpotifySearch {
    Scanner input = new Scanner(System.in);
    private final String DB_NAME = "spotify.db";
    private final String TRACKS_TABLE_NAME = "tracks";

    private final String API_HOST = "spotify23.p.rapidapi.com";
    private final String API_KEY = "753cedf16amshfd615637867c160p115974jsn9f3ac63d0265";

    private Connection conn;
    public KeyReader kr;


    public SpotifySearch() {
        // Connect to SQLite database
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
            createTables();
            System.out.println("Connected!!");
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS " + TRACKS_TABLE_NAME + " (" +
                "id TEXT PRIMARY KEY," +
                "name TEXT," +
                "artist TEXT" +
                ")");
    }

    public void searchArtist(String query) {
        String url = "https://spotify23.p.rapidapi.com/search?q=" + query + "&type=artist";

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 301) {
                String newUrl = response.headers().firstValue("Location").orElse("");
                System.out.println("The resource has been moved permanently to: " + newUrl);
            } else {
                System.out.println(response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void searchTracks(String query) {
        try {
            // Send search request to Spotify API
            String requestUri = "https://spotify23.p.rapidapi.com/search/?q=" + query + "&type=track&offset=0&limit=10&numberOfTopResults=5";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUri))
                    .header("X-RapidAPI-Key", API_KEY)
                    .header("X-RapidAPI-Host", API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            // Parse response JSON to get track information
            JSONObject jsonObject = new JSONObject(response.body());
            JSONObject tracksObject = jsonObject.getJSONObject("tracks");
            JSONArray itemsArray = tracksObject.getJSONArray("items");
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject trackObject = itemsArray.getJSONObject(i);
                String id = trackObject.getString("id");
                String name = trackObject.getString("name");
                String artist = trackObject.getJSONArray("artists").getJSONObject(0).getString("name");

                // Store track information in database
                PreparedStatement statement = conn.prepareStatement("INSERT INTO " + TRACKS_TABLE_NAME + " (id, name, artist) VALUES (?, ?, ?)");
                statement.setString(1, id);
                statement.setString(2, name);
                statement.setString(3, artist);
                statement.executeUpdate();
            }
        } catch (IOException | InterruptedException | SQLException e) {
            e.printStackTrace();
        }
    }
}
