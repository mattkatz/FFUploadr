#!/bin/bash
curbranch=$(git branch | sed -n -e 's/^\* \(.*\)/\1/p')
pushd bugs
ditz html
popd
git add bugs
git commit bugs -m "auto committing ditz html files"
git checkout gh-pages
git checkout $curbranch bugs/html
git add bugs
git commit bugs -m "auto committing ditz html files"
git push
git checkout $curbranch

