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
package org.neo4j.example.retries.resilience4j;

import java.util.logging.Logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import org.neo4j.example.retries.domain.DomainConfig;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;

@SpringBootApplication
@EnableNeo4jRepositories(
		basePackages = { "org.neo4j.example.retries.domain", "org.neo4j.example.retries.resilience4j" })
@Import(DomainConfig.class) // This is only needed as the config is not in the same base
							// package
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Enable logging for resilience4j
	@Bean
	public RegistryEventConsumer<Retry> myRegistryEventConsumer() {

		return new RegistryEventConsumer<>() {
			private final Logger logger = Logger.getLogger("io.github.resilience4j");

			@Override
			public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
				entryAddedEvent.getAddedEntry().getEventPublisher().onRetry(r -> logger.info(r.toString()));
			}

			@Override
			public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {

			}

			@Override
			public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
				entryReplacedEvent.getNewEntry().getEventPublisher().onRetry(r -> logger.info(r.toString()));
			}
		};
	}

}
