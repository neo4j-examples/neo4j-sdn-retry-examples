/*
 * Copyright (c) 2024-2025 "Neo4j,"
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
package org.neo4j.example.retries.resilience4j;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.RepeatedTest;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.neo4j.example.retries.domain.ogm.Movie;
import org.neo4j.example.retries.domain.ogm.Person;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationTests {

	@RepeatedTest(5)
	void usingRetryableService(@Autowired MovieService movieService, @Autowired Driver driver) {

		// Deleting everything with driver retries (deletes will fail with the chaos
		// monkey, too)
		try (var session = driver.session()) {
			session.executeWrite(tx -> tx.run("MATCH (n) DETACH DELETE n").consume());
			var cnt = session
				.executeRead(tx -> tx.run("MATCH (n:Test) RETURN count(n) AS cnt").single().get("cnt").asLong());
			assertThat(cnt).isEqualTo(0);
		}

		var movie = movieService.createMovingStarring(new Person("Helge Schneider", 1955),
				"Der frühe Vogel fängt den Wurm");
		assertThat(movie).isNotNull()
			.extracting(Movie::getActors)
			.asInstanceOf(InstanceOfAssertFactories.LIST)
			.hasSize(1);

		try (var session = driver.session()) {
			var records = session.run("MATCH (p:Person)-[r:ACTED_IN]->(m:Movie) RETURN *").stream().toList();
			assertThat(records).hasSize(1).first().satisfies(record -> {
				assertThat(record.get("p").get("name").asString()).isEqualTo("Helge Schneider");
				assertThat(record.get("r").get("roles").asList(Value::asString)).containsExactly("Starring");
				assertThat(record.get("m").get("title").asString()).isEqualTo("Der frühe Vogel fängt den Wurm");
			});
		}
	}

}
