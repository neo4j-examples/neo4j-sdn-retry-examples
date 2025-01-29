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
package org.neo4j.example.rate_limited.resilience4j;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import io.github.resilience4j.core.exception.AcquirePermissionCancelledException;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterProperties;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ApplicationTests {

	@Test
	void usingRateLimitedService(
		@Autowired RateLimiterProperties rateLimiterProperties,
		@Autowired PeopleService peopleService,
		@Autowired Driver driver
	) throws InterruptedException {

		try (var session = driver.session()) {
			session.executeWrite(tx -> tx.run("MATCH (n) DETACH DELETE n").consume());
			var cnt = session
				.executeRead(tx -> tx.run("MATCH (n:Test) RETURN count(n) AS cnt").single().get("cnt").asLong());
			assertThat(cnt).isEqualTo(0);
		}

		try (ExecutorService executorService = Executors.newWorkStealingPool()) {
			var numThreads = Runtime.getRuntime().availableProcessors();
			var latch = new CountDownLatch(4);
			for (int i = 0; i < numThreads; ++i) {
				executorService.execute(() -> {
					// Wait until all threads are ready to go
					try {
						latch.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					var random = ThreadLocalRandom.current();
					while (!Thread.currentThread().isInterrupted()) {
						try {
							peopleService.createNewPersonViaQuery("RLPerson " + random.nextInt(1000), random.nextInt(1900, 2025));
						} catch (AcquirePermissionCancelledException ignored) {
						}
					}
				});
				latch.countDown();
			}


			var sleep = Duration.ofSeconds(10);
			Thread.sleep(sleep.toMillis());
			executorService.shutdownNow();

			try (var session = driver.session()) {
				var numPeople = session.executeRead(tx -> tx.run("MATCH (n:Person) WHERE n.name STARTS WITH 'RLPerson' RETURN count(n)").single().get(0).asLong());
				assertThat(numPeople).isEqualTo(sleep.toSeconds() * rateLimiterProperties.getLimiters().get("newpeople").getLimitForPeriod());
			}
		}
	}

}
