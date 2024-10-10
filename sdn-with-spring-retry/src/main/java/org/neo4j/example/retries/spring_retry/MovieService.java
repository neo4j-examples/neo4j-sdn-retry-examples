/*
 * Copyright (c) 2024 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.example.retries.spring_retry;

import java.util.List;

import org.neo4j.driver.exceptions.AuthorizationExpiredException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.exceptions.TransientException;
import org.springframework.data.domain.Example;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.neo4j.example.retries.domain.Actor;
import org.neo4j.example.retries.domain.Movie;
import org.neo4j.example.retries.domain.MovieRepository;
import org.neo4j.example.retries.domain.PeopleRepository;
import org.neo4j.example.retries.domain.Person;

@Service
public class MovieService {

	private final PeopleRepository peopleRepository;

	private final MovieRepository movieRepository;

	public MovieService(MovieRepository movieRepository, PeopleRepository peopleRepository) {
		this.movieRepository = movieRepository;
		this.peopleRepository = peopleRepository;
	}

	@Retryable(
			retryFor = { TransientException.class, AuthorizationExpiredException.class, SessionExpiredException.class },
			maxAttempts = 10)
	@Transactional
	public Movie createMovingStarring(Person person, String movieTitle) {
		if (movieRepository.existsById(movieTitle)) {
			throw new RuntimeException("Movie '%s' already exists".formatted(movieTitle));
		}
		var existingPerson = peopleRepository.findOne(Example.of(person))
			.orElseGet(() -> peopleRepository.save(person));
		var movie = new Movie(movieTitle, "n/a", List.of(new Actor(existingPerson, List.of("Starring"))), List.of());
		return movieRepository.save(movie);
	}

}
