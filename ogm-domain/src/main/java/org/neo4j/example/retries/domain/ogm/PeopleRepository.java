package org.neo4j.example.retries.domain.ogm;

import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.session.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Using OGM directly, without going through SDN5s repository abstraction
 */
@Repository
public class PeopleRepository {

	private final Session session;

	public PeopleRepository(Session session) {
		this.session = session;
	}

	@Transactional(readOnly = true)
	public Optional<Person> findOne(Person example) {

		return Optional.ofNullable(session.queryForObject(
			Person.class, """
				MATCH (p:Person)
				WHERE p.name = $name OR $name IS NULL
				  AND p.born = $born OR $born IS NULL
				RETURN p""",
			Map.of("name", example.getName(), "born", example.getBorn())));
	}


	@Transactional
	public Person save(Person person) {
		session.save(person);
		return person;
	}
}
