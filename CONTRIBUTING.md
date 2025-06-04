# Contributing to Spring Starter Java

Thank you for considering contributing to Spring Starter Java! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to abide by our code of conduct. Please be respectful and considerate of others.

## How to Contribute

### Reporting Issues

If you find a bug or have a suggestion for improvement:

1. Check if the issue already exists in the [GitHub Issues](https://github.com/yourusername/spring-starter-java/issues)
2. If not, create a new issue with a clear description and steps to reproduce
3. Include relevant details such as your environment and any error messages

### Submitting Changes

1. Fork the repository
2. Create a new branch from `main` for your changes
3. Make your changes following our coding conventions
4. Write or update tests as needed
5. Ensure all tests pass by running `./gradlew test`
6. Submit a pull request

### Pull Request Process

1. Update the README.md or documentation with details of your changes if needed
2. Update the CHANGELOG.md with a description of your changes
3. The PR will be reviewed by maintainers who may request changes
4. Once approved, your PR will be merged

## Development Setup

1. Clone the repository
2. Install prerequisites (Java 21, Docker, Docker Compose)
3. Run `./gradlew build` to build the project
4. Run `docker-compose up -d` to start the database
5. Run `./gradlew app:runApp` to start the application

## Coding Conventions

- Follow the existing code style
- Write clean, readable code with meaningful variable and method names
- Include appropriate tests for your changes
- Document public APIs
- Keep commits focused and with clear messages

## Testing

- Write unit tests for all new code
- Update existing tests when modifying code
- Ensure all tests pass before submitting a PR
- Run `./gradlew test` to execute all tests

## Documentation

- Update documentation when changing functionality
- Add ADRs for significant architectural decisions
- Keep the README.md up to date

## License

By contributing to this project, you agree that your contributions will be licensed under the project's MIT License. 