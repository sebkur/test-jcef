#!/bin/bash

DIR=$(dirname $0)
LIBS="$DIR/../build/lib-run"

if [ ! -d "$LIBS" ]; then
	echo "Please run './gradlew createRuntime'"
	exit 1
fi

CLASSPATH="$LIBS/*"
NATIVE=$DIR/../native/

exec java -Djava.library.path="$NATIVE" -cp "$CLASSPATH" "$@"