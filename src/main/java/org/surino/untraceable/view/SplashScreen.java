package org.surino.untraceable.view;


import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    private final Stage stage;
    private final ProgressBar progressBar;
    private final Label statusLabel;

    public SplashScreen() {

        stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("""
                -fx-background-color: rgba(245,245,245,0.95);
                -fx-background-radius: 12;
                """);

        // 🔹 Logo
        ImageView logo = new ImageView(
                new Image(getClass().getResource("/img/untreceable-splash.png").toExternalForm())
        );
        logo.setFitHeight(120);
        logo.setPreserveRatio(true);
        BorderPane.setAlignment(logo, Pos.CENTER);

        // 🔹 Label stato
        statusLabel = new Label("Avvio…");

        // 🔹 Progress
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        root.setTop(logo);
        root.setCenter(statusLabel);
        root.setBottom(progressBar);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
    }

    // ================= SHOW SPLASH =================

    public void showSplash() {

        stage.getScene().getRoot().setOpacity(0);
        stage.show();

        FadeTransition fadeIn =
                new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    // ================= CLOSE SPLASH =================

    public void close() {

        FadeTransition fadeOut =
                new FadeTransition(Duration.millis(300), stage.getScene().getRoot());

        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> stage.close());

        fadeOut.play();
    }

    // ================= PROGRESS =================

    public void setProgress(int value, String message) {

        Platform.runLater(() -> {

            statusLabel.setText(message);

            double oldVal = progressBar.getProgress();
            double newVal = Math.max(0, Math.min(1, value / 100.0));

            Timeline anim = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(progressBar.progressProperty(), oldVal)),
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(progressBar.progressProperty(), newVal))
            );

            anim.play();
        });
    }
}
