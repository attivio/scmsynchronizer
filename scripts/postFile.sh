#!/usr/bin/env bash

curl -XPOST -H "Content-Type: application/json" --data @$1 http://localhost:19000/payload

echo Result: $?
