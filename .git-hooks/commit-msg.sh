#!/usr/bin/env bash

commit_pattern="(Merge (remote-tracking )?branch|### What's done:)"
error_msg="Your commit message doesn't match the pattern $commit_pattern. Please fix it."
commit_count="$(git rev-list --count master..HEAD 2> /dev/null)"

if [[ $commit_count = "0" && ! $( cat "$1" ) =~ $commit_pattern ]]
then
    echo "$error_msg"
    exit 1
fi

exit 0
