# Wargames

**Wargames** is a simple strategy game where two rival generals command armies of soldiers and manage a limited supply of gold coins. Each soldier has a military rank and gains experience through maneuvers. Generals can train, recruit new soldiers, and attack the opponent. A presidential secretary observes every action and records detailed reports.

## Project structure

- **Models** – simple domain classes for `Soldier`, `Army` and `General`.
- **Commands** – encapsulated operations such as `RecruitSoldiersCommand` executed by a `General`.
- **Event system** – an `EventDispatcher` singleton notifies `Subscriber`s (e.g. a `Secretary`) before and after each command.
- **Factories** – helper classes for creating soldiers and commands.

```
.
├── LICENSE
├── pom.xml
├── README.md
└── src
    ├── main
    │   └── java
    │       └── wargames
    │           ├── commands
    │           ├── events
    │           │   ├── publisher
    │           │   └── subscribers
    │           ├── exceptions
    │           ├── factories
    │           └── models
    └── test
        └── java
            └── wargames
                ├── commands
                ├── events
                │   ├── publisher
                │   └── subscriber
                ├── exceptions
                ├── integration
                └── models
```

## Building

This is a **Maven** project requiring **Java 21** or later. To compile the code run:

```
mvn clean package
```

This will also generate a **JaCoCo coverage report** in `target/site/jacoco`.

## Running tests

The tests use **JUnit 5**. Run them with:

```
mvn clean test
```

## Continuous Integration

The project includes a **GitHub Actions workflow** (`.github/workflows/maven-ci.yml`) that builds and tests the code on every push or pull request to the `main` branch.

## License

This project is licensed under the [GNU GPLv3](LICENSE).
