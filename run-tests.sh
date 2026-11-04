#!/usr/bin/env bash
# Dependency-free fallback to `mvn test` (needs only a JDK 21 and the JUnit console jar).
set -euo pipefail
JUNIT="${JUNIT_CONSOLE_JAR:-/usr/share/java/junit-platform-console-standalone.jar}"
rm -rf out && mkdir -p out
javac -d out -cp "$JUNIT" $(find src -name '*.java')
java -jar "$JUNIT" execute -cp out --scan-classpath --details=tree --disable-banner
