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
package org.neo4j.example.retries.chaos;

import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransientFailureException;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.kernel.api.exceptions.Status;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

@ServiceProvider
public final class NaiveChaosMonkeyFactory extends ExtensionFactory<NaiveChaosMonkeyFactory.Dependencies> {

	private final Map<String, NaiveChaosMonkey> listeners = new ConcurrentHashMap<>();

	public NaiveChaosMonkeyFactory() {
		super("NaiveChaosMonkey");
	}

	@Override
	public Lifecycle newInstance(ExtensionContext context, NaiveChaosMonkeyFactory.Dependencies dependencies) {

		return new LifecycleAdapter() {

			@Override
			public void start() {
				dependencies.databaseManagementService().listDatabases().forEach(databaseName -> {
					if (!"system".equals(databaseName)) {
						var listener = listeners.computeIfAbsent(databaseName, ignored -> new NaiveChaosMonkey());
						dependencies.databaseManagementService()
							.registerTransactionEventListener(databaseName, listener);
					}
				});
			}

			@Override
			public void stop() {
				dependencies.databaseManagementService().listDatabases().forEach(databaseName -> {
					var listener = listeners.get(databaseName);
					if (listener != null) {
						dependencies.databaseManagementService()
							.unregisterTransactionEventListener(databaseName, listener);
					}
				});
			}
		};
	}

	public interface Dependencies {

		DatabaseManagementService databaseManagementService();

	}

	private static final class NaiveChaosMonkey implements TransactionEventListener<NaiveChaosMonkey.State> {

		private final ThreadLocalRandom random = ThreadLocalRandom.current();

		@Override
		public State beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService) {

			if (random.nextBoolean()) {
				throw new TransientFailureException("¯\\_(ツ)_/¯") {
					@Serial
					private static final long serialVersionUID = 1390673084179426077L;

					@Override
					public Status status() {
						return Status.General.DatabaseUnavailable;
					}
				};
			}
			return State.INSTANCE;
		}

		@Override
		public void afterCommit(TransactionData data, State state, GraphDatabaseService databaseService) {
		}

		@Override
		public void afterRollback(TransactionData data, State state, GraphDatabaseService databaseService) {
		}

		public enum State {

			INSTANCE

		}

	}

}
