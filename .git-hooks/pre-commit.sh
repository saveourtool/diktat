#!/usr/bin/env bash

branch_name="$(git rev-parse --abbrev-ref HEAD)"
branch_pattern="^(feature|bugfix|hotfix|infra)/.*$"
error_message="Your branch name doesn't match the pattern $branch_pattern. Please correct it before committing."

if [[ ! $branch_name =~ $branch_pattern ]]
then
  echo "$error_message"
  exit 1
fi

exit 0
