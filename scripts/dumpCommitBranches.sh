#!/usr/bin/env bash

REVLIST=$1

##
# List commit,branch1,branch2, for all branches which received commit as 
# merge or on which it was initially created
# @param commitId = sha1
##
function getMergedBranchesForCommit {
  commitId=$1

  branches=`git branch -a --contains $commitId --merged|grep remotes|egrep -v 'origin\/HEAD '| sed 's/ *remotes\///'|sed ':a;N;$!ba;s/\n/,/g'`
  echo $commitId,$branches
}

##
# Generates List of all commits
##
function generateCommitList {
  # Generate List of all Commits
  git pull --all
  git rev-list --remotes > $REVLIST
}

# ----------------------------------
# M A I N
# ----------------------------------

generateCommitList

for commitId in `cat $REVLIST` 
do
  getMergedBranchesForCommit $commitId
done

