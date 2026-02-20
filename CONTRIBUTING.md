# Contributing to ApproveJ

First, thank you so much for taking the time to contribute! 🙏


## Code of Conduct

Please note that this project has a [code of conduct](CODE_OF_CONDUCT.md).
By contributing, you are expected to uphold this code.


## How to Contribute

### Reporting Bugs

Before you report a new bug, please check [the current list of issues](https://github.com/mkutz/approvej/issues) to avoid creating duplicates.
If your issue is already reported, please feel free to add additional information to it.

When you [create a new issue](https://github.com/mkutz/approvej/issues/new/choose), please try to include the following information:

1. Describe the problem short and concise.
   What happens? What did you expect to happen?
2. List your tech stack.
   - Which version of ApproveJ? Which modules?
   - Which JDK?
   - If you code in something different from Java, which language in what version?
   - Which operating system?
   - Which build tool?
3. Provide information on how to reproduce the problem.
   Best provide a minimal, complete and verifiable example (see [Stackoverflow's Guidelines](https://stackoverflow.com/help/minimal-reproducible-example)).
   E.g. a JUnit test case.

### Suggest new Features

If you like to see a new feature in ApproveJ, please first check if the idea is already being discussed in the [current discussions](https://github.com/mkutz/approvej/discussions) and rather upvote it instead of creating a new one.
Otherwise, please feel free to [open a new discussion](https://github.com/mkutz/approvej/discussions/new).

Please add the following information in your suggestion:

1. _Why_ do you think the feature is a good addition to ApproveJ?
2. _What_ should be changed, added or removed?
3. (Optional) _How_ do you suggest implementing the feature?

When your idea is thoroughly discussed and accepted, an [issue](https://github.com/mkutz/approvej/issues) will be created.

Please avoid the temptation to work on your feature in the meantime.
It is not impossible that we disagree on the need for the feature.
It is not unlikely that somebody comes up with a different idea to achieve the same thing.
It is likely that there is another way to implement the feature.


### Improve Documentation

If you find any smaller piece of documentation in the [manual](https://approvej.org/) that you'd like to change, please feel free to open a pull request.
Please mind the chapter on the [documentation guidelines below](#documentation).

For bigger changes or if you're not certain, please follow the same process as described [above](#suggest-new-features).


### Providing Code

If you want to provide code, please make sure that there's a consensus on the feature was reached (see [above](#suggest-new-features)) and read the following chapters on [project knowledge](#project-knowledge).

After you're done, please [open a new pull request](https://github.com/mkutz/approvej/compare) and wait for a review by a maintainer.


## Project Knowledge

In oder to prevent avoidable conflicts in pull requests, please read this in case you want to contribute any code to ApproveJ.


### Commit Messages

Each commit message should follow the guidelines in @cbeams article on [How to Write a Git Commit Message](https://cbea.ms/git-commit/).

Each commit message

- is written in present tense ("Add something", not "Added something),
- uses imperative mood ("Move stuff from A to B", not "Moves stuff from A to B"),
- limits the first line (the subject) to 50 characters,
- starts with a capital letter,
- does not end the subject line with a period,
- separates subject from the body (third line and following) with a blank line,
- put any references to issues, pull requests or external links in the body,
- wraps the body after 72 characters (exceptions for URLs or similar),
- explains in the body _what_ and _why_, not _how_.


### Building

The project uses [Gradle](https://gradle.org) for building.
To verify your changes, it should generally be enough to execute

```shell
./gradlew check
```

in the project's root folder.

As continuous integration and deployment tool, the project uses [GitHub Actions](https://github.com/features/actions).
All pipelines can be found in [.github/workflows](.github/workflows).


### Versioning

ApproveJ is using [SemVer 2.0](https://semver.org/spec/v2.0.0.html) but omits the patch digit in case it is `0`.

E.g. `1.0.0` is written as `1.0`, `1.0.1` is written as `1.0.1`, and `1.1.0` is written as `1.1`.


### Project Structure

It is structured in four main directories:

- The [modules](modules) directory contains all the published library modules:
  - [core](modules/core) contains the code for the core framework and should not have any dependencies to other modules and only very few (if any) to external libraries,
  - [json-jackson](modules/json-jackson) contains JSON-related code using Jackson 2.x,
  - [json-jackson3](modules/json-jackson3) contains JSON-related code using Jackson 3.x,
  - [yaml-jackson](modules/yaml-jackson) contains YAML-related code using Jackson 2.x,
  - [yaml-jackson3](modules/yaml-jackson3) contains YAML-related code using Jackson 3.x,
  - [http](modules/http) contains code to create an HTTP server for approving requests,
  - [http-wiremock](modules/http-wiremock) contains the WireMock adapter for HTTP testing
- The [plugins](plugins) directory contains build tool plugins:
  - [approvej-gradle-plugin](plugins/approvej-gradle-plugin) contains the Gradle plugin for managing approved files
  - [approvej-maven-plugin](plugins/approvej-maven-plugin) contains the Maven plugin for managing approved files
- the [bom](bom) directory contains the build file to generate a [Maven Bill of Material (BOM)](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms) for all the ApproveJ modules, and
- the [manual](manual) directory contains the projects documentation written in [AsciiDoc](https://docs.asciidoctor.org/asciidoc/latest/).


### Packages

The main package of the project is `org.approvej`.
Only the [core](modules/core) module may use this package directly.
New modules should generally have an own root package below, e.g. `org.approvej.json.jackson`.

Packages group code that severs a common purpose.
E.g. `org.approvej.print` in the core module contains all code related to printing.


### Immutability

In order to minimize side effects, all classes in the framework should be as immutable as possible.

Hence, wherever possible

- class fields should be declared `final`,
- local variable should be declared `final`,
- fields should be initialized in the constructor or class body.

Note that this principal is currently not being followed in the `core` module in certain `Scrubber`s with more complex and optional configurations.
This compromise was made to simplify the API.


### Visibility

In order to prevent confusion, only API elements that are required by a user should be declared `public`.
Other internally used elements should have the lowest possible visibility:

- internally used classes should be package-private (default visibility),
- fields in `public` classes should be declared `private`,
- only abstract classes should declare `protected` fields.

E.g. the built-in `Scrubber`s have package-private constructors as they are supposed to be initialized via the `Scrubbers` util class only.


### Code Style

The project is written in Java 21 and uses [Spotless](https://github.com/diffplug/spotless) to format the code according to the [Google Java Style](https://google.github.io/styleguide/javaguide.html).

Generally files all code files should

- use two space indentation,
- end with a new line

Please follow [these instructions](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides) to configure your IDE.

To reformat the code, please run

```shell
./gradlew spotlessApply
```

before committing.

You can automate that by adding the provided commit hooks to your Git configuration:

```shell
cp pre-commit .git/hooks/
cp pre-push .git/hooks/
```

### Tests

#### Stack

All tests are written in [JUnit 5](https://junit.org/junit5/) and use exclusively [AssertJ](https://assertj.github.io/doc/) for assertions and assumptions.
Tests may also be declared [parametrized](https://junit.org/junit5/docs/current/user-guide/#writing-tests-parameterized-tests), preferably with a `@ValueSource` or an `@EnumSource`.


#### Assertions

Each test must contain at least one assertion.


#### Test Code Style

In order to keep tests concise, their code should be compact and short.
Hence, within tests,

- local variables should not be declared final,
- parameters should not be declared final.


#### Self-Sufficient

In order to make tests easy to analyse and understand, all information needed should be visible from the test code:

- non-trivial setup steps should be kept in the test itself and
- usage of `@BeforeAll`, `@BeforeEach`, `@AfterEach`, `@AfterAll` and similar should be kept to a technically necessary minimum (e.g. staring/stopping of doubles).


#### Test Method Names & Display Names

In order to avoid confusion due to out-dated method names,

- test method names should start with the method name for more complex classes (e.g. `toString`, `apply`),
- can contain a description how the case differs from the default test case (e.g. `apply_empty_string`),
- avoid trivial words like `should`, `with` or `does` (e.g. `apply_empty_string` instead of `apply_with_empty_string`),
- not state default behaviour, but only exceptions form it (e.g. `toString` instead of `toString returns a human readable string`),
- not exceed 50 characters.

General pattern: `<what is under test>[_<difference to default case>]`

The expected behavior should not be part of the test method name.
It is the most complex part and most likely to change over time.
The test code itself should be clear enough to understand it.


#### Dependencies

In order to avoid dependencies between tests,

- tests should be kept self-sufficient,
- tests may not rely on tear down steps (e.g. `@AfterEach`, `@AfterAll`) to run but must ensure the required state in their own setup phase (e.g. `@BeforeAll`, `@BeforeEach`), and
- dependencies to external services are to be avoided and their availability must be checked with an assumption.


#### Mocks

Using mocks should be avoided.
Instead, real objects should be used where possible.

If infrastructure is required for a test, a [Nullable Infrastructure component](https://www.jamesshore.com/v2/blog/2018/testing-without-mocks#nullable-infrastructure) should be used instead.


### Documentation

#### Manual

Each feature needs to be described in ApproveJ's [AsciiDoc manual](manual).

In order to make changes in Git less confusing, each sentence in the manual AsciiDoc should end with a linebreak.

All code samples should be included from actual tests in [the manual's tests](manual/src/test).


#### Markdown

In order to make changes in Git less confusing, each sentence in the manual Markdown should end with a linebreak.


#### JavaDoc

There must be JavaDoc for the following:

- each public class requires at least a summary,
- each public method requires a summary, its parameters and return value must be documented.

JavaDoc should follow the following rules:

- always link types (e.g. `@param scrubber the {@link Scrubber} applied to the {@linke #value`),
- code is marked with `@code` (e.g. `when {@code agent.does(someTask)} is called`),
- summaries are full sentences and end with a `.`,
- parameter and return type documentation is usually not a full sentence and does not end with a `.`,
- thrown `RuntimeExceptions` should be documented with `@throws` and state the possible reasons.
