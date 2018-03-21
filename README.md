# Akka Tx Exporter

Exports txns from rippled SQLite, converts meta and tx blobs into json, saves it to ordinary MySql db.


### Db parameters
Sqlite and Mysql dbs are defined in `application.conf`

*Sqlite*

| Field        | Description    |   
| ------------- |:-------------:| 
| limit         | the number of rows selected in a query | 
| minLedger     | starting ledger      | 
| maxLedger     | end ledger      | 

It is important that users addresses are available between minLedger and maxLedger. 

*MySql*

The important fields are
- db.default.url
- db.default.user
- db.default.password

### Dependencies

In Idea, go to File->Project Structure->Library, add these in ./lib folder

- ripple-bouncycastle-0.0.1-SNAPSHOT.jar

- ripple-core-0.0.1-SNAPSHOT.jar


### Building and Running

To build, run this in root

```
sbt assembly
```
It will build a fat jar in ./target/scala-xxx/

To run the app needs three arguments
1. File of all users' addresses
2. Folder containing rippled `transaction.db` and `ledger.db`. (these files are in `/var/lib/rippled/db/`)
3. Log folder

```
java -jar xxx.jar <path to user addresses file> <path to rippled's db dir> <path to log dir>
```
