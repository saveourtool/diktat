# UPDATE THE MAIN [README.md](../README.md)

#### To update the codestyle text in the main [README.md](../README.md):

* You **NEED TO CHANGE** the content of the file `guide-chapter-<N>.md`, contained in `info/guide`, the corresponding section of the rules that you changed / added.

* ```console
  $ cd info/
  $ ./gradlew :generateFullDoc
  $ ./gradlew :generateAvailableRules
  ```

#### You **DO NOT NEED TO CHANGE** the content of the [`diktat-coding-convention.md`](guide/diktat-coding-convention.md) file.
