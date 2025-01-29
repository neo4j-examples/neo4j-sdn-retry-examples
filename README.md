# neo4j-sdn-retry-examples

This repository contains a couple of modules:

**Shared**

* `naive-chaos-monkey`: A custom Neo4j transaction listener that makes roughly half of all transaction fail

**Spring Data Neo4j 6+ (SDN) examples** 

* `sdn-domain`: A Spring Data Neo4j domain containing the usual movies and actors, with two repositories as data access objects
* `sdn-with-resilience4j`: A Spring Boot application with SDN and a service class that uses both repositories and has several interactions with the database. The service is made retryable with resilience4j. The SDN6 `RetryExceptionPredicate` is used to determine the retryability of the exception thrown.
* `sdn-rate-limited`: A Spring Boot application with SDN, also using resilience4j but for rate limiting, making sure that Neo4j gets only 10 queries/second
* `sdn-with-spring-retry`: A Spring Boot application with SDN and a service class that uses both repositories and has several interactions with the database. The service is made retryable with spring-retry.

**SDN5+OGM and pure Neo4j-OGM examples**

* `ogm-domain`: A SDN5+OGM domain containing a movie repository based on SDN5 and a person repository that uses OGM directly without the repository abstract
* `ogm-with-spring-resilience4j`: A Spring Boot application with SDN5+OGM and a service class that uses both repositories and has several interactions with the database. The service is made retryable with resilience4j. A custom predicate is used for determining wether an exception is retryable or not.
* `ogm-with-spring-retry`: A Spring Boot application with SDN5+OGM and a service class that uses both repositories and has several interactions with the database. The service is made retryable with spring-retry.

All applications use the same domain, and the same service code, hence the setup is a bit more complex than the default Spring Boot service, in which the domain is seldom separated into another module

Both retry mechanism are ultimately based on an additional Aspect, hence both need to have higher precedence than the transactional interceptor.
The difference is that with spring-retry is that it will automatically unwrap the cause of any exception to look for the exceptions to retry on.
resilience4j does not do that, so we need to use the SDN6+ predicate or a custom to check if something should be retryable.
The predicate is configured in `application.properties`

The setup here includes a naive "chaos monkey" deployed to the database brought up via testcontainers that makes roughly half of all transactions fail during commit.
Never deploy this plugin to production.
