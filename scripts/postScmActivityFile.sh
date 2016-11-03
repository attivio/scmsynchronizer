#!/usr/bin/env bash

curl -s -u "jiraUser:JiraPassword" -XPOST -H "Content-Type: application/json" \
--data @$1 http://localhost:8080/rest/scmactivity/1.0/changeset/activity

echo Result: $?
