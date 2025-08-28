package com.example.uefamediaplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class uefaMediaPlayerController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            loadMedias();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mediaSlider.setFocusTraversable(false);
        mediaButton.setFocusTraversable(false);
        infoButton.setFocusTraversable(false);
        darkOverlay.setVisible(false);
        stackPane.widthProperty().addListener((obs, oldVal, newVal) -> updateMediaViewSize());
        stackPane.heightProperty().addListener((obs, oldVal, newVal) -> updateMediaViewSize());

        setMenuList();
        setupListEvents();
    }

    @FXML
    private ListView<String> menuList;

    @FXML
    private ListView<String> mediaList;

    @FXML
    private MediaView mediaView;

    @FXML
    private ImageView infoPanel;

    @FXML
    private Slider mediaSlider;

    @FXML
    private Button mediaButton;

    @FXML
    private Button infoButton;

    @FXML
    private Rectangle darkOverlay;

    @FXML
    private StackPane stackPane;

    @FXML
    private HBox sideBar;

    @FXML
    private VBox mediaStack;

    private boolean isPlaying = false;  // Épp játszódik-e le videó
    private boolean isShown = false;    // Megvan-e jelenítve videóhoz infókép
    private MediaPlayer mediaPlayer;
    private String currentFile;         // Jelenleg lejátszott videó
    private final Map<String, Map<String, Media>> mediaMap = new HashMap<>();   // Videók kategória szerint rendezve
    private final Map<String, Image> infoMap = new HashMap<>();     // Képek csak fájlnévvel tárolva

    @FXML
    void pauseMedia() {
        // Videó megállítása/ elindítása
        if (mediaPlayer == null) {
            return;
        }

        if (!isPlaying) {
            mediaButton.setText("PLAY");
            mediaPlayer.pause();
            isPlaying = true;
        } else {
            mediaButton.setText("PAUSE");
            mediaPlayer.play();
            isPlaying = false;
            if (isShown) {
                infoPanel.setVisible(false);
                darkOverlay.setVisible(false);
                isShown = false;
            }
        }
    }

    @FXML
    void showInfo() {
        // Videókhoz tartozó képek megjelenítése
        if (mediaPlayer == null) {
            return;
        }

        if (!isShown) {
            infoPanel.setImage(infoMap.get(currentFile));
            infoPanel.setVisible(true);
            isShown = true;
            darkOverlay.setVisible(true);
            if (!isPlaying) {
                pauseMedia();
            }
        } else {
            infoPanel.setVisible(false);
            isShown = false;
            darkOverlay.setVisible(false);
        }
    }

    void loadMedias() throws URISyntaxException {
        // Videó és képanyagok betöltése és eltárolása
        String appDir = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        File videoFolder = new File(appDir, "Resource/medias/clips");
        File decisionFolder = new File(appDir, "Resource/medias/images/decisions");
        File explanationFolder = new File(appDir, "Resource/medias/images/explanations");

        if (videoFolder.exists() && videoFolder.isDirectory()) {
            File[] videos = videoFolder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".mp4")));
            assert videos != null;
            File[] decisions = decisionFolder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".png")));
            assert decisions != null;
            File[] explanations = explanationFolder.listFiles(((dir, name) -> name.toLowerCase().endsWith(".png")));
            assert explanations != null;

            for (File file : decisions) {
                String fileName = stripExtension(file.getName());
                Image image = new Image(file.toURI().toString());
                infoMap.put(fileName, image);
            }

            for (File file : explanations) {
                String fileName = stripExtension(file.getName());
                Image image = new Image(file.toURI().toString());
                infoMap.put(fileName, image);
            }

            for (File file : videos) {
                String fileName = stripExtension(file.getName());
                char prefix = fileName.charAt(0);
                Media media = new Media(file.toURI().toString());
                String category;

                switch (Character.toUpperCase(prefix)) {
                    case 'A': category = "CHALLENGES"; break;
                    case 'B': category = "DOGSO-SPA"; break;
                    case 'C': category = "HANDBALL"; break;
                    case 'D': category = "HOLDING"; break;
                    case 'E': category = "ILLEGAL USE OF ARMS"; break;
                    case 'F': category = "PENALTY AREA DECISIONS"; break;
                    case 'G': category = "SIMULATION"; break;
                    case 'H': category = "ADVANTAGE"; break;
                    case 'J': category = "CONTROL/CONFRONTATION"; break;
                    case 'K': category = "DISSENT"; break;
                    case 'L': category = "OFFSIDE"; break;
                    case 'M': category = "TEAMWORK"; break;
                    case 'N': category = "LAWS OF THE GAME"; break;
                    default: continue;
                }
                mediaMap.computeIfAbsent(category, k -> new HashMap<>()).put(fileName, media);
            }
        }
    }

    void setMenuList() {
        // Menülista beállítása
        menuList.getItems().clear();
        ObservableList<String> options = FXCollections.observableArrayList("CHALLENGES", "DOGSO-SPA", "HANDBALL", "HOLDING", "ILLEGAL USE OF ARMS", "PENALTY AREA DECISIONS", "SIMULATION", "ADVANTAGE", "CONTROL/CONFRONTATION", "DISSENT", "OFFSIDE", "TEAMWORK", "LAWS OF THE GAME");
        menuList.setItems(options);
    }

    void setMediaList(String selected) {
        // Médialista beállítása és rendezése
        mediaList.getItems().clear();
        Map<String, Media> selectedMediaMap = mediaMap.get(selected);
        if (selectedMediaMap != null) {
            ObservableList<String> fileNames = FXCollections.observableArrayList(selectedMediaMap.keySet());
            fileNames.sort((a, b) -> {
                String prefixA = a.replaceAll("[0-9]", "");
                String prefixB = b.replaceAll("[0-9]", "");
                int numA = Integer.parseInt(a.replaceAll("\\D+", ""));
                int numB = Integer.parseInt(b.replaceAll("\\D+", ""));
                int prefixCompare = prefixA.compareTo(prefixB);
                if (prefixCompare != 0) return prefixCompare;
                return Integer.compare(numA, numB);
            });
            mediaList.setItems(fileNames);
        }
    }

    void playVideo(String filename) {
        // Kiválasztott videó lejátszása
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        currentFile = filename;
        infoPanel.setVisible(false);
        darkOverlay.setVisible(false);
        isShown = false;

        String selectedCategory = menuList.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;
        Media selectedMedia = mediaMap.get(selectedCategory).get(filename);
        if (selectedMedia == null) return;

        mediaPlayer = new MediaPlayer(selectedMedia);
        mediaView.setMediaPlayer(mediaPlayer);
        configureMediaSlider(mediaPlayer);

        mediaPlayer.play();
        mediaButton.setText("PAUSE");
        isPlaying = false;

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.seek(Duration.ZERO);
            mediaButton.setText("PLAY");
            isPlaying = true;
            mediaPlayer.pause();
        });
    }

    public void seekSliderBy(double seconds) {
        // mediaSlider manuális változtatásának kezelése
        if (mediaPlayer == null) return;

        Duration currentTime = mediaPlayer.getCurrentTime();
        Duration totalDuration = mediaPlayer.getTotalDuration();

        if (totalDuration == null || totalDuration.isUnknown() || totalDuration.lessThanOrEqualTo(Duration.ZERO)) return;

        Duration seekTime = currentTime.add(Duration.seconds(seconds));

        if (seekTime.lessThan(Duration.ZERO)) {
            seekTime = Duration.ZERO;
        } else if (seekTime.greaterThan(totalDuration) || seekTime.equals(totalDuration)) {
            seekTime = totalDuration;
        }

        mediaPlayer.seek(seekTime);
        mediaSlider.setValue(seekTime.toSeconds());
    }

    void configureMediaSlider(MediaPlayer mp) {
        // mediaSlider beállítása az aktuális videóhoz
        mp.setOnReady(() -> {
            Duration total = mp.getMedia().getDuration();
            mediaSlider.setMax(total.toSeconds());
            mp.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!mediaSlider.isValueChanging()) {
                    mediaSlider.setValue(newTime.toSeconds());
                }
            });
        });

        mediaSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                mp.seek(Duration.seconds(mediaSlider.getValue()));
            }
        });
        mediaSlider.setOnMousePressed(e -> mp.pause());
        mediaSlider.setOnMouseReleased(e -> {
            mp.seek(Duration.seconds(mediaSlider.getValue()));
            if (!isPlaying) {
                mp.play();
                mediaList.requestFocus();
            }
        });
    }

    void setupListEvents() {
        // Listákból való választás egérrel, vagy ENTER billentyűvel
        menuList.setOnMouseClicked(e -> handleMenuSelection());
        menuList.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) handleMenuSelection();
        });
        mediaList.setOnMouseClicked(e -> handleMediaSelection());
        mediaList.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) handleMediaSelection();
        });
    }

    void handleMenuSelection() {
        // Menülistából médialista kiválasztása
        String selected = menuList.getSelectionModel().getSelectedItem();
        if (selected != null) setMediaList(selected);
    }

    void handleMediaSelection() {
        // Médialistából videó kiválasztása
        String selected = mediaList.getSelectionModel().getSelectedItem();
        if (selected != null) playVideo(selected);
    }

    String stripExtension(String fileName) {
        // Kiterjesztések levágása
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex != -1) ? fileName.substring(0, dotIndex) : fileName;
    }

    void updateMediaViewSize() {
        // Felbontás kiszámítása a képanyagok megjelenítéséhez
        double maxW = stackPane.getWidth();
        double maxH = stackPane.getHeight();
        double ratio = 16.0/9.0;

        double width = maxW;
        double height = width / ratio;
        if (height > maxH) {
            height = maxH;
            width = height * ratio;
        }

        mediaView.setFitWidth(width);
        mediaView.setFitHeight(height);
        darkOverlay.setWidth(width);
        darkOverlay.setHeight(height);
        infoPanel.setFitWidth(width);
        infoPanel.setFitHeight(height);
    }
}