#!/usr/bin/env bash

commit_pattern="(Merge (remote-tracking )?branch|### What's done:)"
error_msg="Your commit message doesn't match the pattern $commit_pattern. Please fix it."

if [[ ! $( cat "$1" ) =~ $commit_pattern ]]
then
    echo "$error_msg"
    exit 1
fi

exit 0
