#!/bin/bash
curbranch=$(git branch | sed -n -e 's/^\* \(.*\)/\1/p')
pushd .ditz
ditz html
popd
git add .ditz
git commit .ditz -m "auto committing ditz html files"
git checkout gh-pages
git checkout $curbranch .ditz/html
git add .ditz
git commit .ditz -m "auto committing ditz html files"
git push
git checkout $curbranch

