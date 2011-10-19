#!/bin/sh
mvn install
export REPO=$HOME/.m2/repository
foreman start
