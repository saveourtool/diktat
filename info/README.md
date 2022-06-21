UPDATE MAIN README

* To update the codestyle text in the main README, you **NEED TO CHANGE** the content of the file `guide-chapter-<N>.md`, the corresponding section of the rules that you changed / added.

* After the updates are made **NEED TO PERFORM GENERATION** using gradle `generateFullDoc` and `generateAvailableRules` contained in `/IdeaProjects/diktat/info/build.gradle.kts` file. Example - ` . gradlew :generateFullDoc --continuous` and ` . gradlew :generateAvailableRules --continuous`.

* You **DO NOT NEED TO CHANGE** the contents of the `diktat-coding-convention` file.
