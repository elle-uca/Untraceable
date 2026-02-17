package org.surino.untraceable.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.surino.untraceable.model.Person;
import org.surino.untraceable.model.PersonRepository;
import org.surino.untraceable.model.Status;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

@Service
public class ImportExportService {

    private final PersonRepository personRepository;

    public ImportExportService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    // ================= EXPORT =================

    public void exportToCSV(Stage parent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export People");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(parent);
        if (file == null) return;

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("Name,Surname,Address,Status\n");

            for (Person p : personRepository.findAll()) {
                writer.write(String.format("%s,%s,%s,%s\n",
                        p.getName(),
                        p.getSurname(),
                        p.getAddress() == null ? "" : p.getAddress(),
                        p.getStatus()));
            }

            new Alert(Alert.AlertType.INFORMATION,
                    "Export completed:\n" + file.getAbsolutePath()).showAndWait();

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Error during export: " + e.getMessage()).showAndWait();
        }
    }

    // ================= IMPORT =================

    public List<Person> importFromCSV(Stage parent, String dialogTitle) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(dialogTitle);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(parent);
        if (file == null) return new ArrayList<>();

        List<Person> importedPeople = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.split(",", -1);
                if (parts.length < 4) continue;

                String name = parts[0].trim();
                String surname = parts[1].trim();
                String address = parts[2].trim();

                Status status;
                try {
                    status = Status.valueOf(parts[3].trim());
                } catch (Exception e) {
                    status = Status.SCONOSCIUTO;
                }

                importedPeople.add(new Person(name, surname, address, status));
            }

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Error reading the CSV:\n" + e.getMessage()).showAndWait();
        }

        return importedPeople;
    }

    // ================= IMPORT WITH DUPLICATES =================

    public void importWithDuplicatesCheck(Stage parent, List<Person> importedPeople) {

        if (importedPeople == null || importedPeople.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "No valid records found in the CSV.").showAndWait();
            return;
        }

        List<Person> existingPeople = personRepository.findAll();
        List<Person> toSave = new ArrayList<>();

        int duplicateMode = -1;

        // 🔹 GESTIONE DUPLICATI SUL THREAD FX
        for (Person candidate : importedPeople) {

            List<Person> duplicates = existingPeople.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(candidate.getName())
                            && p.getSurname().equalsIgnoreCase(candidate.getSurname()))
                    .toList();

            if (!duplicates.isEmpty()) {

                if (duplicateMode == -1) {

                    int choice = askDuplicate(parent, candidate, duplicates);

                    if (choice == 1) continue;
                    if (choice == 2) duplicateMode = 0;
                    if (choice == 3) { duplicateMode = 1; continue; }

                } else if (duplicateMode == 1) {
                    continue;
                }
            }

            toSave.add(candidate);
        }

        // 🔹 TASK SOLO PER SALVATAGGIO
        Stage progressStage = createProgressDialog(parent);
        ProgressBar progressBar =
                (ProgressBar) progressStage.getScene().lookup("#progressBar");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {

                for (int i = 0; i < toSave.size(); i++) {
                    personRepository.save(toSave.get(i));
                    updateProgress(i + 1, toSave.size());
                }

                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            progressStage.close();
            new Alert(Alert.AlertType.INFORMATION,
                    "Import completed. Imported: " + toSave.size())
                    .showAndWait();
        });

        new Thread(task).start();
        progressStage.show();
    }
    
    private int askDuplicate(Stage parent, Person candidate, List<Person> duplicates) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(parent);
        alert.setTitle("Duplicate Found");
        alert.setHeaderText(candidate.getName() + " " + candidate.getSurname());

        String dupInfo = duplicates.stream()
                .map(p -> "- " + p.getName() + " " + p.getSurname())
                .collect(Collectors.joining("\n"));

        alert.setContentText("Existing entries:\n" + dupInfo);

        ButtonType importOne = new ButtonType("Import anyway");
        ButtonType skipOne = new ButtonType("Skip duplicate");
        ButtonType importAll = new ButtonType("Import all");
        ButtonType skipAll = new ButtonType("Skip all");

        alert.getButtonTypes().setAll(importOne, skipOne, importAll, skipAll);

        ButtonType result = alert.showAndWait().orElse(skipOne);

        if (result == importOne) return 0;
        if (result == skipOne) return 1;
        if (result == importAll) return 2;
        if (result == skipAll) return 3;

        return 1;
    }

    private Stage createProgressDialog(Stage parent) {
        Stage stage = new Stage();
        stage.initOwner(parent);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Importing...");

        Label label = new Label("Importing people...");
        ProgressBar bar = new ProgressBar(0);
        bar.setId("progressBar");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setTop(label);
        root.setCenter(bar);

        stage.setScene(new Scene(root, 400, 120));

        return stage;
    }
}