#!/bin/bash
set -ev

mvn -Drevision=1.0.3.$TRAVIS_BUILD_NUMBER-SNAPSHOT -Dbuildnumber=1 clean package javadoc:jar source:jar deploy

