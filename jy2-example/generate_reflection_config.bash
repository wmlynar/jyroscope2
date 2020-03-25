#!/bin/bash

# https://medium.com/graalvm/introducing-the-tracing-agent-simplifying-graalvm-native-image-configuration-c3b56c486271

# install agent with command:
# gu install native-image

mkdir -p src/main/resources/META-INF/native-image
java -agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image -jar target/jy2-example-0.0.9-SNAPSHOT.jar

