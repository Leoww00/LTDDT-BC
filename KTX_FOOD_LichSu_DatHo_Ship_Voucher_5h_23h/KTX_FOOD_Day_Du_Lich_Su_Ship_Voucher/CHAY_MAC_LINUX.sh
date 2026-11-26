#!/bin/sh
cd "$(dirname "$0")" || exit 1
mkdir -p out
find src -name '*.java' > sources.txt
javac -encoding UTF-8 -d out @sources.txt || exit 1
java -cp out main.Main
