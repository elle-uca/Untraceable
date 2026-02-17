package org.surino.untraceable.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("""
           SELECT p FROM Person p
           WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%'))
              OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :text, '%'))
           ORDER BY p.surname ASC, p.name ASC
           """)
    Page<Person> searchByNameOrSurname(
            @Param("text") String text,
            Pageable pageable
    );
}       