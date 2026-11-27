#!/bin/bash
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out com.lms.Main
