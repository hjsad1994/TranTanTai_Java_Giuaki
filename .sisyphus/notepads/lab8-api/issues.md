# Lab 8 API - Issues

## Problems & Gotchas

(Empty - will document issues encountered)

## Task 4 Blocker: Spring Boot 4.0.2 Incompatibility

### Issue
Cannot create @WebMvcTest tests as required - Spring Boot 4.0.2 has removed/relocated these annotations:
- `@WebMvcTest` - package org.springframework.boot.test.autoconfigure.web.servlet does not exist
- `@MockBean` - package org.springframework.boot.test.mock.mockito does not exist

### Root Cause
Project uses Spring Boot 4.0.2 (extremely new, possibly pre-release) which has breaking API changes.
Test annotations that worked in Spring Boot 2.x/3.x no longer exist in the same packages.

### Evidence
```
[ERROR] package org.springframework.boot.test.autoconfigure.web.servlet does not exist
[ERROR] package org.springframework.boot.test.mock.mockito does not exist
```

Dependencies show:
- spring-boot-starter-test:4.0.2 is present
- spring-security-test:7.0.2 is present  
- But @WebMvcTest and @MockBean classes don't exist in these jars

### Attempted Solutions
1. Added spring-boot-starter-test dependency - already present
2. Added spring-security-test dependency - already present
3. Added spring-boot-starter-webmvc-test dependency - exists but doesn't help
4. Tried @SpringBootTest + @AutoConfigureMockMvc - @MockBean still missing

### Impact
- Cannot complete Task 4 (BookApiControllerTest) as specified
- CategoryApiControllerTest also broken for same reason
- Plan explicitly requires @WebMvcTest and @MockBean annotations

### Workaround Options
1. Downgrade Spring Boot to 3.x (requires pom.xml changes)
2. Use plain Mockito without Spring test annotations (different approach than specified)
3. Wait for Spring Boot 4.x stable release with proper test support

### Recommendation
This is a project-level configuration issue, not a code issue. Either:
- Downgrade to Spring Boot 3.3.x (last stable version)
- Update task requirements to match Spring Boot 4.x testing approach (if documented)
- Mark task as blocked until environment is fixed

Date: 2026-02-05
Status: BLOCKED
