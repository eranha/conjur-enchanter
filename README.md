# conjur-enchanter
A Java 11 Swing GUI to Conjur.
The purpose pf the tool is to stream line the use of conjur, and for training.

## Build and package the application
Run `mvn clean compile assembly:single` to build the project and package it as an executable jar file.

## Run the application
NOTE! By default the conjur-enchanter trusts all certificates, 
to override this behaviour and run in a secure mode, execute the application with `-Dignore.certs=false`

To run conjur-enchanter execute the following in a terminal window:
`java -jar conjur-enchanter-1.0-SNAPSHOT-jar-with-dependencies` 
