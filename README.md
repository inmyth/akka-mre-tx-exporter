## Akka Tx Exporter

Exports txns from SQLite, converts meta and tx blobs into json, saves it to ordinary MySql db.


### Dependencies

In Idea, File->Project Structure->Library, add these in ./lib folder

-ripple-bouncycastle-0.0.1-SNAPSHOT.jar
-ripple-core-0.0.1-SNAPSHOT.jar


### Build and run

Run this in project folder
```
sbt assembly
```
Resulting jar is in ./target

```
java -jar thejar.jar <path to user addresses file> <path to rippled's sqlite transaction.db> <path to log>

```
