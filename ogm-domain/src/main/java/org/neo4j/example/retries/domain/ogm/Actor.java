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
package org.neo4j.example.retries.domain.ogm;

import java.util.Collections;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity("ACTED_IN")
public final class Actor {

	@Id
	@GeneratedValue
	private Long id;

	@StartNode
	private Movie movie;

	@EndNode
	private Person person;

	private final List<String> roles;

	public Actor(Movie movie, Person person, List<String> roles) {
		this.movie = movie;
		this.person = person;
		this.roles = roles;
	}

	public List<String> getRoles() {
		return Collections.unmodifiableList(roles);
	}

}