package org.surino.untraceable.view;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.surino.untraceable.model.Person;
import org.surino.untraceable.model.PersonRepository;
import org.surino.untraceable.model.Status;
import org.surino.untraceable.service.ImportExportService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

@Component
public class PersonView extends BorderPane {

	private final PersonRepository personRepository;
	private final ImportExportService importExportService;

	private TextField nameField;
	private TextField surnameField;
	private TextField addressField;
	private TextField notesField;
	private ComboBox<Status> statusCombo;
	private TextField searchField;

	private TableView<Person> table;
	private ObservableList<Person> masterData;
	private FilteredList<Person> filteredData;

	public PersonView(PersonRepository personRepository,
			ImportExportService importExportService) {
		this.personRepository = personRepository;
		this.importExportService = importExportService;

		initUI();
		loadPeople();
	}

	private void initUI() {

		/* ================= FORM ================= */
		nameField = new TextField();
		surnameField = new TextField();
		addressField = new TextField();
		notesField = new TextField();
		statusCombo = new ComboBox<>(
				FXCollections.observableArrayList(Status.values()));
		statusCombo.getSelectionModel().selectFirst();

		GridPane.setHgrow(nameField, Priority.ALWAYS);
		GridPane.setHgrow(surnameField, Priority.ALWAYS);
		GridPane.setHgrow(addressField, Priority.ALWAYS);
		GridPane.setHgrow(notesField, Priority.ALWAYS);

		nameField.setMaxWidth(Double.MAX_VALUE);
		surnameField.setMaxWidth(Double.MAX_VALUE);
		addressField.setMaxWidth(Double.MAX_VALUE);
		notesField.setMaxWidth(Double.MAX_VALUE);

		GridPane form = new GridPane();
		form.setHgap(10);
		form.setVgap(10);
		form.setPadding(new Insets(10));

		form.add(new Label("Surname:"),	0, 0);
		form.add(surnameField, 			1, 0);
		form.add(new Label("Name:"), 	2, 0);
		form.add(nameField, 			3, 0);
		form.add(new Label("Address:"), 0, 1);
		form.add(addressField, 			1, 1);
		form.add(new Label("Note:"), 	2, 1);
		form.add(notesField, 			3, 1);        
		form.add(new Label("Status:"), 	0, 2);
		form.add(statusCombo, 			1, 2);        

		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setPercentWidth(6);

		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setPercentWidth(42);
		fieldCol.setHgrow(Priority.ALWAYS);

		form.getColumnConstraints().setAll(
				labelCol, fieldCol,
				labelCol, fieldCol
				);

		Button saveBtn   = new Button("💾 Save");
		Button deleteBtn = new Button("🗑 Delete Selected");
		Button importBtn = new Button("📥 Import CSV");
		Button exportBtn = new Button("📤 Export CSV");

		saveBtn.setOnAction(e -> savePerson());
		deleteBtn.setOnAction(e -> deletePerson());
		importBtn.setOnAction(e -> importCSV());
		exportBtn.setOnAction(e -> exportCSV());

		HBox buttons = new HBox(10, saveBtn, deleteBtn, importBtn, exportBtn);
		form.add(buttons, 0, 4, 4, 1);

		setTop(form);

		/* ================= SEARCH ================= */
		searchField = new TextField();
		searchField.setPromptText("🔍 Search...");
		searchField.setPadding(new Insets(5));


		/* ================= TABLE ================= */
		table = new TableView<>();
		table.setEditable(true);
		table.setColumnResizePolicy(
				TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<Person, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		nameCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setName(e.getNewValue());
			personRepository.save(p);
		});

		TableColumn<Person, String> surnameCol = new TableColumn<>("Surname");
		surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
		surnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		surnameCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setSurname(e.getNewValue());
			personRepository.save(p);
		});

		TableColumn<Person, String> addressCol = new TableColumn<>("Address");
		addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
		addressCol.setCellFactory(TextFieldTableCell.forTableColumn());
		addressCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setAddress(e.getNewValue());
			personRepository.save(p);
		});

		TableColumn<Person, Status> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(Status.values()));
		statusCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setStatus(e.getNewValue());
			personRepository.save(p);
		});

		TableColumn<Person, String> notesCol = new TableColumn<>("Notes");
		notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
		notesCol.setCellFactory(TextFieldTableCell.forTableColumn());
		notesCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setNotes(e.getNewValue());
			personRepository.save(p);
		});

		table.getColumns().setAll(List.of(
				surnameCol, nameCol, addressCol, statusCol, notesCol) );
		table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		ContextMenu contextMenu = new ContextMenu();

		// --- Elimina ---
		MenuItem deleteItem = new MenuItem("Elimina");
		deleteItem.setOnAction(e -> deletePerson());

		// --- Scambia Nome/Cognome ---
		MenuItem swapItem = new MenuItem("Scambia Nome/Cognome");
		swapItem.setOnAction(e -> {
			Person p = table.getSelectionModel().getSelectedItem();
			if (p != null) {

				String tmp = p.getName();
				p.setName(p.getSurname());
				p.setSurname(tmp);

				personRepository.save(p);

				table.refresh(); // importante!
			}
		});

		contextMenu.getItems().addAll(deleteItem, swapItem);

		// assegna il menu alla tabella
		table.setContextMenu(contextMenu);


		VBox centerBox = new VBox(5, searchField, table);
		centerBox.setPadding(new Insets(0, 10, 10, 10));
		VBox.setVgrow(table, Priority.ALWAYS);
		setCenter(centerBox);
	}

	private void loadPeople() {
		masterData = FXCollections.observableArrayList(personRepository.findAll());
		filteredData = new FilteredList<>(masterData, p -> true);
		SortedList<Person> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sortedData);
		searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));
	}

	private void applyFilter(String filter) {
		String f = filter.toLowerCase();
		filteredData.setPredicate(p ->
		f.isEmpty()
		|| p.getName().toLowerCase().contains(f)
		|| p.getSurname().toLowerCase().contains(f)
		|| (p.getAddress() != null && p.getAddress().toLowerCase().contains(f))
				);
	}

	private void savePerson() {
		String name = nameField.getText().trim();
		String surname = surnameField.getText().trim();
		String address = addressField.getText().trim();
		Status status = statusCombo.getValue();
		String notes = notesField.getText().trim();

		if (name.isEmpty() || surname.isEmpty()) {
			alert(Alert.AlertType.ERROR, "Validation error", 
					"Name and surname are required.");
			return;
		}

		List<Person> duplicates = personRepository.findAll().stream()
				.filter(p -> p.getName().equalsIgnoreCase(name)
						&& p.getSurname().equalsIgnoreCase(surname))
				.collect(Collectors.toList());

		if (!duplicates.isEmpty()) {
			String msg = duplicates.stream()
					.map(p -> "- " + p.getName() + " " + p.getSurname())
					.collect(Collectors.joining("\n"));

			Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
			confirm.setTitle("Duplicate detected");
			confirm.setHeaderText("A person with the same name already exists:");
			confirm.setContentText(msg + "\n\nSave anyway?");

			if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.OK) {
				return;
			}
		}

		personRepository.save(new Person(name, surname, address, notes, status));

		nameField.clear();
		surnameField.clear();
		addressField.clear();
		statusCombo.getSelectionModel().selectFirst();
		notesField.clear();
		loadPeople();
	}

	private void deletePerson() {
		Person p = table.getSelectionModel().getSelectedItem();
		if (p == null) {
			alert(Alert.AlertType.WARNING, "Delete", "Select a person first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete " + p.getName() + " " + p.getSurname() + "?",
				ButtonType.YES, ButtonType.NO);

		if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
			personRepository.delete(p);
			loadPeople();
		}
	}

	private void importCSV() {
		List<Person> imported = importExportService.importFromCSV(null, "Import People");
		importExportService.importWithDuplicatesCheck(null, imported);
		loadPeople();
	}

	private void exportCSV() {
		importExportService.exportToCSV(null);
	}

	private void alert(Alert.AlertType type, String title, String msg) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}

