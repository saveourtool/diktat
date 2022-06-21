UPDATE MAIN README

* To update the code style text in the main README, you **NEED TO CHANGE** the contents of the file `guide-chapter-n.md` , the paragraph in which you added/changed the rule contained in `/IdeaProjects/diktat/info/`.

* After the updates you **NEED TO PERFORM GENERATION** using gradle `generateFullDoc` and `generateAvailableRules` contained in `/IdeaProjects/diktat/info/build.gradle.kts`. Example - ` . gradlew :generateFullDoc --continuous` and ` . gradlew :generateAvailableRules --continuous`.

* You **DO NOT NEED TO CHANGE** the contents of the `diktat-coding-convention` file.
