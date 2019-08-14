#!/bin/bash
set -ev

mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean package javadoc:jar source:jar compile
#mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean package javadoc:jar source:jar deploy

