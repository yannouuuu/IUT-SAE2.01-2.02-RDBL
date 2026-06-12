package sae.transport.comparison;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppFX extends Application {

    private static final double DEFAULT_WIDTH  = 1050;
    private static final double DEFAULT_HEIGHT = 750;
    private static final String WINDOW_TITLE = "Comparaison de Transport";

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/sae/transport/comparison/fxml/home-view.fxml")
        );
        Parent root = loader.load();
        scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        stage.setTitle(WINDOW_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(750);
        AppState.getInstance().appliquerTheme(root, AppState.getInstance().getThemeColor(), AppState.getInstance().getDarkMode());
        stage.show();

        // Enregistrer la fenêtre principale dans l'état partagé
        AppState.getInstance().setPrimaryStage(stage);
    }

    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}