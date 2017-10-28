#!/usr/bin/env bash
git checkout -b anewbranch
git rm -r --cached app/keystore.properties
git rm -r --cached app/crashlytics.properties
git rm -r --cached app/config.properties
git rm -r --cached app/key
git rm -r --cached app/google-services.json
git rm -r --cached app/src/main/res/values/config.xml
git commit -m "Basic commit"

git reset $(git commit-tree HEAD^{tree} -m "Initial commit")

git push --force public refs/heads/anewbranch:master

git checkout master
git branch -D anewbranch