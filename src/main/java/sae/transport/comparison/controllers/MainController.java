package sae.transport.comparison.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class MainController {

    @FXML
    private TextField departField;

    @FXML
    private TextField arriveeField;

    @FXML
    private HBox dropZone;

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            dropZone.getStyleClass().add("drag-over");
        }
        event.consume();
    }

    @FXML
    private void handleDragExited(DragEvent event) {
        dropZone.getStyleClass().remove("drag-over");
        event.consume();
    }

    @FXML
    private void handleDragDropped(DragEvent event) {
        dropZone.getStyleClass().remove("drag-over");
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            success = true;
            System.out.println("Fichiers déposés : " + event.getDragboard().getFiles());
            // TODO: traiter le fichier CSV déposé
        }
        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleDropZoneClick(MouseEvent event) {
        System.out.println("Zone de dépôt cliquée - ouvrir le sélecteur de fichiers");
        // TODO: ouvrir le FileChooser
    }

    @FXML
    private void handleCreateCsv() {
        System.out.println("Créer un fichier CSV cliqué");
        // TODO: ouvrir l'interface de création
    }
}
