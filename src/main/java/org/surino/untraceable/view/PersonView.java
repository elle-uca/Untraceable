package org.surino.untraceable.view;

import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.stereotype.Component;
import org.surino.untraceable.model.Person;
import org.surino.untraceable.model.Status;
import org.surino.untraceable.service.ImportExportService;
import org.surino.untraceable.service.PersonService;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

@Component
public class PersonView extends BorderPane {

	private final ImportExportService importExportService;
	private final PersonService personService;

	private TextField nameField;
	private TextField surnameField;
	private TextField addressField;
	private TextField notesField;
	private ComboBox<Status> statusCombo;
	private TextField searchField;

	private TableView<Person> table;
	private ObservableList<Person> masterData;
	private FilteredList<Person> filteredData;

	public PersonView(
			ImportExportService importExportService, 
			PersonService personService) {
		
		this.importExportService = importExportService;
		this.personService = personService;

		initUI();
		setupSearch();
		//loadPeople();
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
		GridPane.setHgrow(statusCombo, Priority.ALWAYS);
		GridPane.setHgrow(notesField, Priority.ALWAYS);
		
		nameField.setMaxWidth(Double.MAX_VALUE);
		surnameField.setMaxWidth(Double.MAX_VALUE);
		addressField.setMaxWidth(Double.MAX_VALUE);
		notesField.setMaxWidth(Double.MAX_VALUE);
		statusCombo.setMaxWidth(Double.MAX_VALUE);
		
		/* ================= SEARCH ================= */
		searchField = new TextField();
		searchField.setPromptText("🔍 Search...");
		//searchField.setPadding(new Insets(5));


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
		form.add(searchField, 			0, 4, 4, 1);        

		ColumnConstraints labelCol = new ColumnConstraints();
		labelCol.setPercentWidth(8);

		ColumnConstraints fieldCol = new ColumnConstraints();
		fieldCol.setPercentWidth(41);
		fieldCol.setHgrow(Priority.ALWAYS);

		form.getColumnConstraints().setAll(
				labelCol, fieldCol,
				labelCol, fieldCol
				);
		setTop(new VBox(createToolbar(), form));
		

		VBox centerBox = new VBox(5, createTable());
		centerBox.setPadding(new Insets(0, 10, 10, 10));
		VBox.setVgrow(table, Priority.ALWAYS);
		setCenter(centerBox);
	}
	
	private void setupSearch() {
	    PauseTransition pause = new PauseTransition(Duration.millis(300));
	    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
	        pause.setOnFinished(e -> {
	            String text = newVal == null ? "" : newVal.trim();

	            if (text.length() < 3) {
	            	table.getItems().clear();
	                return;
	            }

	            List<Person> results = personService.search(text);
	            table.setItems(
	                    FXCollections.observableArrayList(results)
	            );
	        });
	        pause.playFromStart();
	    });
	}
	
	private TableView<Person> createTable() {
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
			personService.save(p);
		});

		TableColumn<Person, String> surnameCol = new TableColumn<>("Surname");
		surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
		surnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		surnameCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setSurname(e.getNewValue());
			personService.save(p);
		});

		TableColumn<Person, String> addressCol = new TableColumn<>("Address");
		addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
		addressCol.setCellFactory(TextFieldTableCell.forTableColumn());
		addressCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setAddress(e.getNewValue());
			personService.save(p);
		});

		TableColumn<Person, Status> statusCol = new TableColumn<>("Status");
		statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
		statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(Status.values()));
		statusCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setStatus(e.getNewValue());
			personService.save(p);
		});

		TableColumn<Person, String> notesCol = new TableColumn<>("Notes");
		notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
		notesCol.setCellFactory(TextFieldTableCell.forTableColumn());
		notesCol.setOnEditCommit(e -> {
			Person p = e.getRowValue();
			p.setNotes(e.getNewValue());
			personService.save(p);
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

				personService.save(p);
				table.refresh(); // importante!
			}
		});

		contextMenu.getItems().addAll(deleteItem, swapItem);

		// assegna il menu alla tabella
		table.setContextMenu(contextMenu);
		return table;
	}
	
	
	private ToolBar createToolbar() {
	    Button saveBtn = createIconButton("mdi2c-content-save", "Save");
	    saveBtn.getStyleClass().add("toolbar-save");
	    saveBtn.setOnAction(e -> savePerson());

	    Button deleteBtn = createIconButton("mdi2d-delete", "Delete selected");
	    deleteBtn.getStyleClass().add("toolbar-delete");
	    deleteBtn.setOnAction(e -> deletePerson());

	    Button importBtn = createIconButton("mdi2f-file-import", "Import CSV");
	    importBtn.getStyleClass().add("toolbar-neutral");
	    importBtn.setOnAction(e -> importCSV());

	    Button exportBtn = createIconButton("mdi2f-file-export", "Export CSV");
	    exportBtn.getStyleClass().add("toolbar-neutral");
	    exportBtn.setOnAction(e -> exportCSV());

	    ToolBar toolBar = new ToolBar(
	            saveBtn,
	            deleteBtn,
	            new Separator(),
	            importBtn,
	            exportBtn
	    );

	    toolBar.getStyleClass().add("app-toolbar");
	    return toolBar;
	}

	private Button createIconButton(String iconLiteral, String tooltipText) {
	    FontIcon icon = new FontIcon(iconLiteral);
	    icon.setIconSize(18);

	    Button btn = new Button();
	    btn.setGraphic(icon);
	    btn.setTooltip(new Tooltip(tooltipText));
	    btn.getStyleClass().add("toolbar-button");

	    return btn;
	}

//	private void loadPeople() {
//		masterData = FXCollections.observableArrayList(personService.findAllLimited());
//		filteredData = new FilteredList<>(masterData, p -> true);
//		SortedList<Person> sortedData = new SortedList<>(filteredData);
//		sortedData.comparatorProperty().bind(table.comparatorProperty());
//		table.setItems(sortedData);
//		searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));
//	}

//	private void applyFilter(String filter) {
//		String f = filter.toLowerCase();
//		filteredData.setPredicate(p ->
//		f.isEmpty()
//		|| p.getName().toLowerCase().contains(f)
//		|| p.getSurname().toLowerCase().contains(f)
//		|| (p.getAddress() != null && p.getAddress().toLowerCase().contains(f))
//				);
//	}

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

		List<Person> duplicates = personService.findAllLimited().stream()
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

		personService.save(new Person(name, surname, address, notes, status));

		nameField.clear();
		surnameField.clear();
		addressField.clear();
		statusCombo.getSelectionModel().selectFirst();
		notesField.clear();
	}

	private void deletePerson() {
		personService.delete(table.getSelectionModel().getSelectedItem());
	}
	
	private Stage getStage() {
	    return (Stage) getScene().getWindow();
	}

	
	private void importCSV() {
	    List<Person> imported =
	            importExportService.importFromCSV(getStage(), "Import People");
	    importExportService.importWithDuplicatesCheck(getStage(), imported);
	    //loadPeople();
	}

	private void exportCSV() {
	    importExportService.exportToCSV(getStage());
	}

	private void alert(Alert.AlertType type, String title, String msg) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}

