# How to release a new version of diKTat

* You should have permissions to push to the main repo
* Simply create a new git tag with format `v*` and push it. Github workflow will perform release automatically.
  
  For example:
  ```bash
  $ git tag v1.0.0
  $ git push origin --tags 
  ```
  
After the release workflow has started, version number is determined from tag. Binaries are uploaded to maven repo and 
a new github release is created with fat jar.
