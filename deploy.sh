#!/bin/bash
set -ev
mvn -s settings.xml clean install deploy

# mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean org.jacoco:jacoco-maven-plugin:prepare-agent package javadoc:jar source:jar compile install sonar:sonar -Dsonar.projectKey=ANRGenstar_genstar


#mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean package javadoc:jar source:jar compile
#mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean package javadoc:jar source:jar deploy
#mvn -Drevision=1.0.4.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=$TRAVIS_BUILD_NUMBER clean package javadoc:jar source:jar deploy
