#!/usr/bin/env sh
set -eax
export JAVA_HOME=$(pwd)/../../graalvm-jdk-23.0.1+11.1
mvn package
CP=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath")
# JVM
$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir
# JVM + JS
$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir \
    js \
    '(f) => { if (f.size > 1000000) { return "Hello EPFL! " + f.name } else { return null } }'
# JVM + Python
$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir \
    python \
    'lambda f : f.name + " -> " + str(f.size) if f.size > 100000 and f.name.startswith("c") else None'
# Native image build
$JAVA_HOME/bin/native-image -cp $CP com.example.ExtListDir
# Native image
./com.example.extlistdir
# Native image + JS
./com.example.extlistdir js '(f) => { if (f.size > 1000000) { return "Hello EPFL! " + f.name} else {return null}}'
# Native image + python
./com.example.extlistdir python 'lambda f : f.name + " -> " + str(f.size) if f.size > 100000 and f.name.startswith("c") else None'
