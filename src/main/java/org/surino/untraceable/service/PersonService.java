package org.surino.untraceable.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.surino.untraceable.model.Person;
import org.surino.untraceable.model.PersonRepository;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }


    
    // 🔹 CREATE / UPDATE
    public Person  save(Person person) {
        return  personRepository.save(person);
    }

    // 🔹 DELETE
    public void delete(Person person) {
		if (person == null) {
			alert(Alert.AlertType.WARNING, "Delete", "Select a person first.");
			return;
		}

		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete " + person.getName() + " " + person.getSurname() + "?",
				ButtonType.YES, ButtonType.NO);

		if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
			personRepository.delete(person);
		}
    }

    // 🔹 SEARCH
    public List<Person> search(String text) {
        if (text == null || text.length() < 3) {
            return List.of();
        }

        return personRepository
                .searchByNameOrSurname(text, PageRequest.of(0, 200))
                .getContent();
    }

    public List<Person> findDuplicates(String name, String surname) {
        return personRepository
                .findByNameIgnoreCaseAndSurnameIgnoreCase(name, surname);
    }
    
    // 🔹 Optional: find all (limitato)
    public List<Person> findAllLimited() {
        return personRepository
                .findAll(PageRequest.of(0, 200))
                .getContent();
    }
    
	private void alert(Alert.AlertType type, String title, String msg) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(null);
		a.setContentText(msg);
		a.showAndWait();
	}
}