#!/bin/bash
mkdir -p ./app/src/main/graphql/org/aerogear/android/app/memeolist/graphql
apollo-codegen download-schema http://localhost:8000/graphql --output ./app/src/main/graphql/org/aerogear/android/app/memeolist/graphql/schema.json

