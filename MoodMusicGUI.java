import java.awt.*;
import java.io.*;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URI;
import java.util.*;
import javax.swing.*;
import java.util.Map;

    public class MoodMusicGUI extends JFrame {
        private static final Map<String, String> moodToQuery = new HashMap<String, String>();
        private RoundedPanel mainPanel;
        private JLabel titleLabel;
        private JComboBox<String> moodCombo;
        private JTextField customMoodField;
        private JButton fetchButton;
        private JList<String> songList;
        private DefaultListModel<String> listModel;
        private JButton playButton;
        private JButton favoriteButton;
        private JButton showFavoritesButton;
        private JButton shareButton;
        private JButton detailsButton;
        private JSlider volumeSlider;
        private JButton themeSwitchButton;
        private JLabel bgLabel;
        private JLabel nowPlayingLabel;
        private java.util.List<Song> songs;
        private int selectedSongIndex = -1;
        private Set<Song> favorites = new HashSet<>();
        private LinkedList<Song> recentlyPlayed = new LinkedList<>();
        private boolean showingFavorites = false;
        private boolean darkTheme = true;

        static {
            moodToQuery.put("Happy", "happy");
            moodToQuery.put("Sad", "sad");
            moodToQuery.put("Energetic", "energetic");
            moodToQuery.put("Relaxed", "relaxing");
        }

        public MoodMusicGUI() {
            setTitle("Mood-Based Music Recommender");
            setSize(700, 500);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            // Gradient background panel
            JPanel gradientPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    Color color1 = new Color(44, 62, 80);
                    Color color2 = new Color(52, 152, 219);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            gradientPanel.setLayout(new BorderLayout());
            setContentPane(gradientPanel);

            mainPanel = new RoundedPanel(30, new Color(30,30,30,220));
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
            mainPanel.setOpaque(false);

            titleLabel = new JLabel("Mood-Based Music Recommender", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
            titleLabel.setForeground(new Color(0xFFD700));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(titleLabel);

            JPanel moodPanel = new JPanel();
            moodPanel.setOpaque(false);
            moodPanel.setLayout(new BoxLayout(moodPanel, BoxLayout.X_AXIS));
            moodCombo = new JComboBox<>(moodToQuery.keySet().toArray(new String[0]));
            moodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            moodCombo.setMaximumSize(new Dimension(180, 36));
            moodCombo.setBackground(new Color(40, 40, 40));
            moodCombo.setForeground(new Color(0xFFD700));
            customMoodField = new JTextField();
            customMoodField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            customMoodField.setMaximumSize(new Dimension(180, 36));
            customMoodField.setBackground(new Color(40, 40, 40));
            customMoodField.setForeground(new Color(0xFFD700));
            customMoodField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xFFD700)), "Custom Mood"));
            moodPanel.add(moodCombo);
            moodPanel.add(Box.createHorizontalStrut(10));
            moodPanel.add(customMoodField);
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(moodPanel);

            fetchButton = new JButton("Get Songs");
            fetchButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
            fetchButton.setBackground(new Color(0xFFD700));
            fetchButton.setForeground(Color.BLACK);
            fetchButton.setFocusPainted(false);
            fetchButton.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
            fetchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(fetchButton);

            listModel = new DefaultListModel<>();
            songList = new JList<>(listModel);
            songList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            songList.setBackground(new Color(40, 40, 40));
            songList.setForeground(new Color(0xFFD700));
            songList.setBorder(BorderFactory.createLineBorder(new Color(0xFFD700), 1));
            JScrollPane scrollPane = new JScrollPane(songList);
            scrollPane.setPreferredSize(new Dimension(400, 120));
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(Box.createVerticalStrut(20));
            mainPanel.add(scrollPane);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            playButton = new JButton("Play & Visual");
            playButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            playButton.setBackground(new Color(0xFFD700));
            playButton.setForeground(Color.BLACK);
            playButton.setFocusPainted(false);
            playButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            playButton.setEnabled(false);
            buttonPanel.add(playButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            favoriteButton = new JButton("Favorite");
            favoriteButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            favoriteButton.setBackground(new Color(0xFFD700));
            favoriteButton.setForeground(Color.BLACK);
            favoriteButton.setFocusPainted(false);
            favoriteButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            favoriteButton.setEnabled(false);
            buttonPanel.add(favoriteButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            showFavoritesButton = new JButton("Show Favorites");
            showFavoritesButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            showFavoritesButton.setBackground(new Color(0xFFD700));
            showFavoritesButton.setForeground(Color.BLACK);
            showFavoritesButton.setFocusPainted(false);
            showFavoritesButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            buttonPanel.add(showFavoritesButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            shareButton = new JButton("Share");
            shareButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            shareButton.setBackground(new Color(0xFFD700));
            shareButton.setForeground(Color.BLACK);
            shareButton.setFocusPainted(false);
            shareButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            shareButton.setEnabled(false);
            buttonPanel.add(shareButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            detailsButton = new JButton("Details");
            detailsButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            detailsButton.setBackground(new Color(0xFFD700));
            detailsButton.setForeground(Color.BLACK);
            detailsButton.setFocusPainted(false);
            detailsButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            detailsButton.setEnabled(false);
            buttonPanel.add(detailsButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            themeSwitchButton = new JButton("Switch Theme");
            themeSwitchButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            themeSwitchButton.setBackground(new Color(0xFFD700));
            themeSwitchButton.setForeground(Color.BLACK);
            themeSwitchButton.setFocusPainted(false);
            themeSwitchButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            buttonPanel.add(themeSwitchButton);
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(buttonPanel);

            volumeSlider = new JSlider(0, 100, 50);
            volumeSlider.setBackground(new Color(30,30,30,220));
            volumeSlider.setForeground(new Color(0xFFD700));
            volumeSlider.setMajorTickSpacing(25);
            volumeSlider.setPaintTicks(true);
            volumeSlider.setPaintLabels(true);
            volumeSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xFFD700)), "Volume (UI only)"));
            mainPanel.add(Box.createVerticalStrut(10));
            mainPanel.add(volumeSlider);

            bgLabel = new JLabel();
            bgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            bgLabel.setVerticalAlignment(SwingConstants.CENTER);
            gradientPanel.add(bgLabel, BorderLayout.CENTER);
            gradientPanel.add(mainPanel, BorderLayout.NORTH);

            nowPlayingLabel = new JLabel("", SwingConstants.CENTER);
            nowPlayingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            nowPlayingLabel.setForeground(new Color(0xFFD700));
            nowPlayingLabel.setOpaque(false);
            nowPlayingLabel.setBackground(new Color(0,0,0,120));
            nowPlayingLabel.setVisible(false);
            gradientPanel.add(nowPlayingLabel, BorderLayout.SOUTH);

            fetchButton.addActionListener(e -> fetchSongsAction());
            songList.addListSelectionListener(e -> {
                selectedSongIndex = songList.getSelectedIndex();
                boolean valid = selectedSongIndex != -1 && songs != null && selectedSongIndex < songs.size();
                playButton.setEnabled(valid);
                favoriteButton.setEnabled(valid);
                shareButton.setEnabled(valid);
                detailsButton.setEnabled(valid);
            });
            playButton.addActionListener(e -> playSongAction());
            favoriteButton.addActionListener(e -> toggleFavorite());
            showFavoritesButton.addActionListener(e -> showFavorites());
            shareButton.addActionListener(e -> shareSong());
            detailsButton.addActionListener(e -> showDetails());
            themeSwitchButton.addActionListener(e -> switchTheme());
        }

        // RoundedPanel class for rounded corners and drop shadow
        static class RoundedPanel extends JPanel {
            private int cornerRadius;
            private Color bgColor;
            public RoundedPanel(int radius, Color bgColor) {
                super();
                this.cornerRadius = radius;
                this.bgColor = bgColor;
                setOpaque(false);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
                // Drop shadow
                g2.setColor(new Color(44, 62, 80, 40));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, cornerRadius, cornerRadius);
            }
        }

        private void fetchSongsAction() {
            String mood = customMoodField.getText().trim().isEmpty() ? (String) moodCombo.getSelectedItem() : customMoodField.getText().trim();
            listModel.clear();
            songs = fetchSongs(mood);
            if (songs.isEmpty()) {
                listModel.addElement("No songs found for this mood.");
                playButton.setEnabled(false);
            } else {
                for (Song song : songs) {
                    listModel.addElement(song.title + " by " + song.artist);
                }
                playButton.setEnabled(false);
            }
            showingFavorites = false;
            setBackgroundImage(mood);
        }

        private void playSongAction() {
            if (selectedSongIndex < 0 || selectedSongIndex >= songs.size()) return;
            Song song = songs.get(selectedSongIndex);
            // Show 'Now Playing' overlay
            nowPlayingLabel.setText("Now Playing: " + song.title + " by " + song.artist);
            nowPlayingLabel.setVisible(true);
            setBackgroundImage(song.title);
            try {
                Desktop.getDesktop().browse(new URI(song.previewUrl.replace("\\/", "/")));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to open preview: " + ex.getMessage());
            }
            // Add to recently played
            recentlyPlayed.addFirst(song);
            if (recentlyPlayed.size() > 10) recentlyPlayed.removeLast();
            // Hide 'Now Playing' after a short delay (optional)
            new javax.swing.Timer(5000, evt -> nowPlayingLabel.setVisible(false)).start();
        }

        private void toggleFavorite() {
            if (selectedSongIndex < 0 || selectedSongIndex >= songs.size()) return;
            Song song = songs.get(selectedSongIndex);
            if (favorites.contains(song)) {
                favorites.remove(song);
                favoriteButton.setText("Favorite");
            } else {
                favorites.add(song);
                favoriteButton.setText("Unfavorite");
            }
        }

        private void showFavorites() {
            listModel.clear();
            if (!showingFavorites) {
                for (Song song : favorites) {
                    listModel.addElement(song.title + " by " + song.artist);
                }
                showingFavorites = true;
                showFavoritesButton.setText("Show All");
            } else {
                if (songs != null) {
                    for (Song song : songs) {
                        listModel.addElement(song.title + " by " + song.artist);
                    }
                }
                showingFavorites = false;
                showFavoritesButton.setText("Show Favorites");
            }
        }

        private void shareSong() {
            if (selectedSongIndex < 0 || selectedSongIndex >= songs.size()) return;
            Song song = songs.get(selectedSongIndex);
            StringSelection selection = new StringSelection(song.previewUrl);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            JOptionPane.showMessageDialog(this, "Preview link copied to clipboard!");
        }

        private void showDetails() {
            if (selectedSongIndex < 0 || selectedSongIndex >= songs.size()) return;
            Song song = songs.get(selectedSongIndex);
            // For demo, only show title, artist, preview URL
            JOptionPane.showMessageDialog(this,
                "Title: " + song.title +
                "\nArtist: " + song.artist +
                "\nPreview: " + song.previewUrl +
                "\nRecently Played: " + recentlyPlayed.size() +
                "\nFavorite: " + (favorites.contains(song) ? "Yes" : "No"),
                "Song Details", JOptionPane.INFORMATION_MESSAGE);
        }

        private void switchTheme() {
            darkTheme = !darkTheme;
            Color bg = darkTheme ? new Color(30,30,30,220) : new Color(255,255,255,220);
            Color fg = darkTheme ? new Color(0xFFD700) : new Color(44,62,80);
            mainPanel.bgColor = bg;
            titleLabel.setForeground(fg);
            moodCombo.setBackground(darkTheme ? new Color(40,40,40) : new Color(236,240,241));
            moodCombo.setForeground(fg);
            customMoodField.setBackground(darkTheme ? new Color(40,40,40) : new Color(236,240,241));
            customMoodField.setForeground(fg);
            songList.setBackground(darkTheme ? new Color(40,40,40) : new Color(236,240,241));
            songList.setForeground(fg);
            songList.setBorder(BorderFactory.createLineBorder(fg, 1));
            fetchButton.setBackground(fg);
            fetchButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            playButton.setBackground(fg);
            playButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            favoriteButton.setBackground(fg);
            favoriteButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            showFavoritesButton.setBackground(fg);
            showFavoritesButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            shareButton.setBackground(fg);
            shareButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            detailsButton.setBackground(fg);
            detailsButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            themeSwitchButton.setBackground(fg);
            themeSwitchButton.setForeground(darkTheme ? Color.BLACK : Color.WHITE);
            volumeSlider.setBackground(bg);
            volumeSlider.setForeground(fg);
            nowPlayingLabel.setForeground(fg);
            mainPanel.repaint();
        }

        private void setBackgroundImage(String moodOrTitle) {
            // For demo, use a simple mapping to online images
            String imgUrl = null;
            if (moodOrTitle.toLowerCase().contains("happy")) {
                imgUrl = "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=700&q=80";
            } else if (moodOrTitle.toLowerCase().contains("sad")) {
                imgUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=700&q=80";
            } else if (moodOrTitle.toLowerCase().contains("energetic")) {
                imgUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=700&q=80";
            } else if (moodOrTitle.toLowerCase().contains("relax")) {
                imgUrl = "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?auto=format&fit=crop&w=700&q=80";
            } else {
                imgUrl = "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=700&q=80";
            }
            try {
                ImageIcon icon = new ImageIcon(new URL(imgUrl));
                Image img = icon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                bgLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                bgLabel.setIcon(null);
            }
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
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Song song = (Song) o;
                return Objects.equals(title, song.title) && Objects.equals(artist, song.artist);
            }
            @Override
            public int hashCode() {
                return Objects.hash(title, artist);
            }
        }

        private static java.util.List<Song> fetchSongs(String moodQuery) {
            java.util.List<Song> songs = new ArrayList<>();
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
                // Ignore errors for demo
            }
            return songs;
        }

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                MoodMusicGUI gui = new MoodMusicGUI();
                gui.setVisible(true);
            });
        }
    }
