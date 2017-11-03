#!/bin/sh

GRADLE_PROPERTIES="./gradle.properties"
export GRADLE_PROPERTIES
echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

if [ ! -f "$GRADLE_PROPERTIES" ]; then
    echo "Gradle Properties does not exist"

    echo "Creating Gradle Properties file..."
    touch $GRADLE_PROPERTIES
fi

echo "Writing MAVEN_CREDENTIALS to gradle.properties..."
echo "mavenUser=$CAUSECODE_MAVEN_USER" >> $GRADLE_PROPERTIES
echo "mavenPassword=$CAUSECODE_MAVEN_PASSWORD" >> $GRADLE_PROPERTIES
echo "mavenPublishUser=$CAUSECODE_MAVEN_USER" >> $GRADLE_PROPERTIES
echo "mavenPublishPassword=$CAUSECODE_MAVEN_PASSWORD" >> $GRADLE_PROPERTIES