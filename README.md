# Akka Tx Exporter

Exports txns from rippled SQLite, converts meta and tx blobs into json, saves it to ordinary MySql db.


### Db parameters
Sqlite and Mysql dbs are defined in `application.conf`

**Sqlite**

| Field        | Description    |   
| -------------|:-------------: |
| limit        | the number of rows selected in a query |
| minLedger    | starting ledger |
| maxLedger    | end ledger |

It is important that users addresses are available between minLedger and maxLedger. 

**MySql**

The important fields are
- db.default.url
- db.default.user
- db.default.password

### Dependencies

In Idea, go to File->Project Structure->Library, add these in ./lib folder

- ripple-bouncycastle-0.0.1-SNAPSHOT.jar

- ripple-core-0.0.1-SNAPSHOT.jar


### To Use
Open `application.conf` and define all db parameters.
For every operation, ideally current minLedger should be the last maxLedger.

To build and package, run in root

```
sbt assembly
```
The jar will be created in ./target/scala-xxx/

Prepare these
1. File of all users' addresses
2. Folder containing rippled `transaction.db` and `ledger.db`. (these files are from rippled server `/var/lib/rippled/db/`)
3. Log folder

Run with command
```
java -jar xxx.jar <path to user addresses file> <path to rippled's db dir> <path to log dir>
```
