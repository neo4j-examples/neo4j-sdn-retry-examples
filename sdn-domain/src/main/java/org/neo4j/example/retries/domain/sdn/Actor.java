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
package org.neo4j.example.retries.domain.sdn;

import java.util.Collections;
import java.util.List;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public final class Actor {

	@Id
	@GeneratedValue
	private String id;

	@TargetNode
	private final Person person;

	private final List<String> roles;

	public Actor(Person person, List<String> roles) {
		this.person = person;
		this.roles = roles;
	}

	public Person getPerson() {
		return person;
	}

	public String getName() {
		return person.getName();
	}

	public List<String> getRoles() {
		return Collections.unmodifiableList(roles);
	}

}