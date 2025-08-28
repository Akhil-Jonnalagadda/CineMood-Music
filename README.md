
# Mood-Based Music Recommender

A Java Swing application that recommends music based on your selected mood and lets you preview songs with a beautiful, cinematic UI.

## Features
- Select your mood (Happy, Sad, Energetic, Relaxed) or enter a custom mood
- Get a list of recommended songs for your mood
- Preview songs directly in your browser
- Mark songs as favorites and view your favorite list
- See recently played songs in the details popup
- Share song preview links (copy to clipboard)
- View song details in a popup
- Adjust volume (UI only)
- Switch between light and dark themes
- Enjoy a visually appealing, movie-inspired interface with mood-based backgrounds

## How It Works
- The app uses the Deezer API to fetch songs matching your selected mood.
- When you select a mood or enter a custom mood and click "Get Songs", the app displays a list of songs.
- Select a song and use the buttons to play, favorite, share, or view details.

## How to Use
1. **Requirements:**
   - Java 8 or higher
   - Internet connection (for fetching songs and images)
2. **Run the Application:**
   - Compile the Java files:
     ```
     javac MoodBasedMusicRecommender.java MoodMusicGUI.java
     ```
   - Run the GUI:
     ```
     java MoodMusicGUI
     ```
3. **Usage:**
   - Select your mood from the dropdown or type a custom mood.
   - Click "Get Songs" to fetch recommendations.
   - Select a song and use the buttons to play, favorite, share, or view details.
   - Use "Switch Theme" to toggle between light and dark modes.

## Customization
- You can add more moods or change the UI colors by editing `MoodMusicGUI.java`.

## Troubleshooting
- If no songs appear, check your internet connection.
- If previews do not open, ensure your default browser is set and accessible.

## License
This project is for educational/demo purposes.
