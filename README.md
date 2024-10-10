# neo4j-sdn-retry-examples

This repository contains four modules:

* `naive-chaos-monkey`: A custom Neo4j transaction listener that makes roughly half of all transaction fail
* `sdn-domain`: A Spring Data Neo4j domain containing the usual movies and actors, with two repositories as data access objects
* `sdn-with-resilience4j`: A Spring Boot application with SDN and a service class that uses both repositories and has several interactions with the database. The service is made retryable with resilience4j.
* `sdn-with-spring-retry`: A Spring Boot application with SDN and a service class that uses both repositories and has several interactions with the database. The service is made retryable with spring-retry.

Both applications use the same domain, and the same service code.
Both retry mechanism are ultimately based on an additional Aspect, hence both need to have higher precedence than the transactional interceptor.
The difference is that with spring-retry is that it will automatically unwrap the cause of any exception to look for the exceptions to retry on.
resilience4j does not do that, so we need to use the SDN predicate to check if something should be retryable.
That however is an advantage due to a bug in SDN prior to 7.2.11, 7.3.5 and 7.4.0-M2, which would swallow any retryable exception coming from the driver (Here's the change in question: https://github.com/spring-projects/spring-data-neo4j/commit/ddf29146a3cc4dd8aeb024d12a61a02cfcca90cf)
