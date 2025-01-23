package org.neo4j.example.retries.resilience4j;

import java.util.function.Predicate;

import org.neo4j.driver.exceptions.RetryableException;

public final class IsRetryableException implements Predicate<Throwable> {
	@Override
	public boolean test(Throwable throwable) {
		do {
			if (throwable instanceof RetryableException) {
				return true;
			}
		} while ((throwable = throwable.getCause()) != null);
		return false;
	}
}
