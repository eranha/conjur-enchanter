# conjur-enchanter

A Java Swing interface to Conjur.
By default the conjur-enchanter trusts all certificates. 
To override this behaviour run the application with `-Dignore.certs=false`

# Build and package the application
Run `mvn clean compile assembly:single` to build the projct and package it as an executable jar file.

# Run the application
`java -jar conjur-enchanter-1.0-SNAPSHOT-jar-with-dependencies` 
