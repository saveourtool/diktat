## Building
To build and test the plugin, use regular maven commands.

To generate plugin descriptor using data from KDocs, we use [kotlin-maven-plugin-tools](https://github.com/gantsign/kotlin-maven-plugin-tools).
This plugin is only available in github packages, which require authentication via `settings.xml`. However,
this plugin is activated only in release profile (`-Prelease`) and package repository doesn't require any authentication for local development.
If you need to run it locally, see [release.yml](../.github/workflows/release.yml) as an example of adding an entry to `settings.xml` `servers` section.