#!/bin/sh

CLASSPATH=.
for jar in $(ls lib/*.jar); do
	CLASSPATH=$CLASSPATH:$jar
done

JAVA=java
if [ -n "$JAVA_HOME" ]; then
	JAVA=$JAVA_HOME/bin/java
fi

$JAVA -cp $CLASSPATH com.entermedia.ui.ConsoleClient $@
