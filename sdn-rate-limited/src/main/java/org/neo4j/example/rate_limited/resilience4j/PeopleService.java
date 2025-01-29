package org.neo4j.example.rate_limited.resilience4j;

import java.util.Optional;

import org.neo4j.example.retries.domain.sdn.PeopleRepository;
import org.neo4j.example.retries.domain.sdn.Person;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@Service
public class PeopleService {

	private final PeopleRepository peopleRepository;

	public PeopleService(PeopleRepository peopleRepository) {
		this.peopleRepository = peopleRepository;
	}

	@RateLimiter(name = "newpeople", fallbackMethod = "doNothing")
	public Optional<Person> createNewPersonViaQuery(String name, Integer born) {
		return Optional.of(peopleRepository.createNewPersonViaQuery(name, born));
	}

	private Optional<Person> doNothing(String name, Integer born, RequestNotPermitted e) {
		System.err.printf("Could not create person %s: %s%n", name, e.getMessage());
		return Optional.empty();
	}
}
