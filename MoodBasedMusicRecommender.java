import java.io.*;
import java.net.*;
import java.util.*;

public class MoodBasedMusicRecommender {
    // ANSI escape codes for colors (supported in Windows 10+)
    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String YELLOW = "\u001B[33m";
    public static final String GREEN = "\u001B[32m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BLUE = "\u001B[34m";
    public static final String RED = "\u001B[31m";
    private static final Map<String, String> moodToQuery = new HashMap<>();
    static {
        moodToQuery.put("Happy", "happy");
        moodToQuery.put("Sad", "sad");
        moodToQuery.put("Energetic", "energetic");
        moodToQuery.put("Relaxed", "relaxing");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(BLUE + "==============================" + RESET);
        System.out.println(CYAN + "   Mood-Based Music Recommender" + RESET);
        System.out.println(BLUE + "==============================" + RESET);
        System.out.println();
        System.out.println(YELLOW + "How are you feeling today?" + RESET);
        List<String> moods = new ArrayList<>(moodToQuery.keySet());
        for (int i = 0; i < moods.size(); i++) {
            System.out.println(GREEN + "  " + (i + 1) + RESET + ". " + moods.get(i));
        }
        System.out.println();
        System.out.print(MAGENTA + "Enter the number of your mood: " + RESET);
        int choice = 0;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println(RED + "Invalid input. Exiting." + RESET);
            return;
        }
        if (choice < 1 || choice > moods.size()) {
            System.out.println(RED + "Invalid choice. Exiting." + RESET);
            return;
        }
        String selectedMood = moods.get(choice - 1);
        System.out.println();
        System.out.println(BLUE + "Fetching recommended songs for mood: " + YELLOW + selectedMood + RESET + "...");
        List<Song> songs = fetchSongs(moodToQuery.get(selectedMood));
        if (songs.isEmpty()) {
            System.out.println(RED + "No songs found for this mood." + RESET);
            return;
        }
        System.out.println();
        System.out.println(CYAN + String.format("%-5s %-40s %-25s", "No.", "Title", "Artist") + RESET);
        System.out.println(BLUE + "---------------------------------------------------------------" + RESET);
        for (int i = 0; i < songs.size(); i++) {
            System.out.println(String.format(GREEN + "%-5d" + RESET + " %-40s %-25s", (i + 1), songs.get(i).title, songs.get(i).artist));
        }
        System.out.println();
        System.out.print(MAGENTA + "Enter the number of the song to play preview (or 0 to exit): " + RESET);
        int songChoice = 0;
        try {
            songChoice = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println(RED + "Invalid input. Exiting." + RESET);
            return;
        }
        if (songChoice < 1 || songChoice > songs.size()) {
            System.out.println(YELLOW + "Exiting." + RESET);
            return;
        }
        String previewUrl = songs.get(songChoice - 1).previewUrl.replace("\\/", "/");
        System.out.println(BLUE + "Opening preview in your browser..." + RESET);
        openWebpage(previewUrl);
        System.out.println(GREEN + "Enjoy your music!" + RESET);
    }

    static class Song {
        String title;
        String artist;
        String previewUrl;
        Song(String title, String artist, String previewUrl) {
            this.title = title;
            this.artist = artist;
            this.previewUrl = previewUrl;
        }
    }

    private static List<Song> fetchSongs(String moodQuery) {
        List<Song> songs = new ArrayList<>();
        try {
            String apiUrl = "https://api.deezer.com/search?q=" + URLEncoder.encode(moodQuery, "UTF-8");
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // Parse JSON manually (simple parsing for preview)
            String json = response.toString();
            int idx = 0;
            while ((idx = json.indexOf("\"title\":", idx)) != -1 && songs.size() < 5) {
                int titleStart = json.indexOf('"', idx + 8) + 1;
                int titleEnd = json.indexOf('"', titleStart);
                String title = json.substring(titleStart, titleEnd);
                int artistIdx = json.indexOf("\"name\":", titleEnd);
                int artistStart = json.indexOf('"', artistIdx + 7) + 1;
                int artistEnd = json.indexOf('"', artistStart);
                String artist = json.substring(artistStart, artistEnd);
                int previewIdx = json.indexOf("\"preview\":", artistEnd);
                int previewStart = json.indexOf('"', previewIdx + 10) + 1;
                int previewEnd = json.indexOf('"', previewStart);
                String previewUrl = json.substring(previewStart, previewEnd);
                songs.add(new Song(title, artist, previewUrl));
                idx = previewEnd;
            }
        } catch (Exception e) {
            System.out.println("Error fetching songs: " + e.getMessage());
        }
        return songs;
    }

    private static void openWebpage(String urlString) {
        try {
            URI uri = new URI(urlString);
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            System.out.println("Unable to open browser: " + e.getMessage());
        }
    }
}
