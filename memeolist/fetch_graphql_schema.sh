#!/bin/bash
mkdir -p ./app/src/main/graphql/org/aerogear/android/app/memeolist/graphql
apollo-codegen download-schema https://api.graph.cool/simple/v1/cjiyvc1wa40kg011846ev0ff8 --output ./app/src/main/graphql/org/aerogear/android/app/memeolist/graphql/schema.json

