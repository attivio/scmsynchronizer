#!/usr/bin/env bash

if [ "${password}" == "" ]; then
  echo "must set password     export password=secret"
  exit -1
fi

id="bee5ecdba383a0fa394b061217f6e4ef8a9418b7"
url_base="https://github.com/api/v3"
org="organizationName"
repo="repoName"
method="commits"

url="${url_base}/repos/${org}/${repo}/${method}/${id}"

curl -s -u "automationUserName:${password}" -i 'User-Agent: ' ${url} 

echo Result: $?
