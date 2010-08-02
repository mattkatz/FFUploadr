#!/bin/bash
pushd bugs
ditz html
popd
git commit bugs -m "auto committing ditz html files"
git checkout gh-pages
git checkout master bugs/html
git commit bugs -m "auto committing ditz html files"
git push
git checkout master

