## Building
To generate plugin descriptor using data from KDocs, we use [kotlin-maven-plugin-tools]().
It is activated only in release profile (`-Prelease`) and shouldn't be a problem for local development.
Still, this plugin is only available in github packages and needs authentication via `settings.xml`.
If you need to run it locally, see [release.yml](../.github/workflows/release.yml) as an example.