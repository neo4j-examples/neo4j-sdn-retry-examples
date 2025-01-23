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
package org.neo4j.example.retries.spring_retry;

import org.neo4j.example.retries.domain.ogm.DomainConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {"org.neo4j.example.retries.domain.ogm", "org.neo4j.example.retries.spring_retry"})
@Import(DomainConfig.class) // This is only needed as the config is not in the same base package
@EnableRetry(order = Ordered.LOWEST_PRECEDENCE - 10) // Must have higher precedence than @Transactional
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
