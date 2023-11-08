# Contributing

If you are reading this - then you have decided to contribute to our project. Oh, poor you...
Rules are very simple:
1. Fork this repository to your own account
2. Make your changes and verify that tests pass (or wait that our CI/CD will do everything for you)
3. Commit your work and push to a new branch on your fork
4. Submit a pull request
5. Participate in the code review process by responding to feedback

# Technical part

Main components are:
1) diktat-rules — number of rules that are supported by diKTat;
2) diktat-common-test — util methods for functional/unit tests that can be used for running your code fixer on the initial code and compare it with the expected result;
3) also see our demo: diktat-demo in a separate repository.

Mainly we wanted to create a common configurable mechanism that
will give us a chance to enable/disable and customize all rules.
That's why we added logic for:
1) Parsing `.yml` file with configurations of rules and passing it to visitors;
2) Passing information about properties to visitors.
   This information is very useful, when you are trying to get,
   for example, a filename of file where the code is stored;
3) We added a bunch of visitors, checkers and fixers that will extended KTlint functionaliity with code style rules;
4) We have proposed a code style for Kotlin language.

Before you make a pull request, make sure the build is clean as we have lot of tests and other prechecks:

```bash
$ mvn clean install
```

# Hooks

We have some hooks to a commit messages:
1) your commit message should have the following format:
```
Brief Description

### What's done:
1) Long description
2) Long description
```

2) Please also do not forget to update documentation on Wiki after the merge approval and before merge.
