#!/bin/bash

set -e

MAVEN_VERSION="3.9.12"
MAVEN_HOME="${HOME}/.m2/maven-${MAVEN_VERSION}"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven $MAVEN_VERSION..."
  mkdir -p "${HOME}/.m2"
  cd "${HOME}/.m2"
  if ! command -v curl &> /dev/null; then
    echo "curl not found, trying wget..."
    wget "$MAVEN_URL" -O "apache-maven-${MAVEN_VERSION}-bin.zip"
  else
    curl -fsSL "$MAVEN_URL" -o "apache-maven-${MAVEN_VERSION}-bin.zip"
  fi
  unzip -q "apache-maven-${MAVEN_VERSION}-bin.zip"
  rm "apache-maven-${MAVEN_VERSION}-bin.zip"
  cd -
fi

"$MAVEN_HOME/bin/mvn" "$@"
