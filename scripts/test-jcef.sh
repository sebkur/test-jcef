#!/bin/bash

DIR=$(dirname $0)
LIBS="$DIR/../build/lib-run"

if [ ! -d "$LIBS" ]; then
	echo "Please run './gradlew createRuntime'"
	exit 1
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/jvm/java-11-openjdk-amd64/lib/

CLASSPATH="$LIBS/*"
NATIVE=$DIR/../native/

exec java -Djava.library.path="$NATIVE" -cp "$CLASSPATH" "$@"
