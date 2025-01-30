# Embedding languages in GraalVM

## Links

### GraalVM:

- https://www.graalvm.org

### Truffle
- https://www.graalvm.org/latest/reference-manual/embed-languages/
- https://www.graalvm.org/truffle/javadoc/index.html

### Oracle MLE
- https://medium.com/graalvm/mle-executing-javascript-in-oracle-database-c545feb1a010
- https://docs-uat.us.oracle.com/en/database/oracle/oracle-database/23/mlejs/

### Internships
- https://www.graalvm.org/community/internship/

## Commands
- Set GraalVM as `JAVA_HOME` 
    - `export JAVA_HOME=/path/to/graalvm-jdk-23.0.1+11.1`
- Build the project
    - `mvn package`
- To get the classpath
    - `CP=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath")`
- To run ExtListDir
    - `$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir`
        - With js lambda `$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir js '(f) => { if (f.size > 1000000) { return "Hello EPFL! " + f.name} else {return null}}'`
        - with python lambda : `$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir python 'lambda f : f.name + " -> " + str(f.size) if f.size > 100000 and f.name.startswith("c") else None'`
- To Build the native image of ExtListDir
    - `$JAVA_HOME/bin/native-image -cp $CP com.example.ExtListDir`
    - With js lambda `$JAVA_HOME/bin/java -cp $CP com.example.ExtListDir js '(f) => { if (f.size > 1000000) { return "Hello EPFL! " + f.name} else {return null}}'`
    - with python lambda : `./com.example.extlistdir python 'lambda f : f.name + " -> " + str(f.size) if f.size > 100000 and f.name.startswith("c") else None'`
