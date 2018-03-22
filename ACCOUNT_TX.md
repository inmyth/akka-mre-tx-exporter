# account_tx with Sqlite db

### rippled databases

All transactions are stored in RocksDb and Sqlite. Rocksdb stores ledger structure for traversal. 
Sqlite stores indexed data. 

`account_tx` only works if the ledgers are available in server_info's complete_ledgers. It most likely uses Rocksdb.

`tx` always works even if the ledger containing the transaction is gone. It most likely uses Sqlite. 

All databases by default are stored in `/var/lib/rippled/db/` . `.db` are Sqlite files.  
 
To replicate account_tx only Transaction.db is needed. But to allow query with date, another database Ledger.db is needed.  

#### Transaction.db

Transaction.db contains two tables Transactions and AccountTransactions. 
Transactions contains all transactions. 
AccountTransactions contains transactions belonging to accounts. This is technically account_tx

**Transactions**

| Name        | Description    |   
| -------------|:-------------: |
| TransId (PK) | transaction id hash |
| TransType    | transaction type (Payment, OfferCreate,...) |
| FromAcct    | account that creates the transaction, appears in tx field |
| FromSeq     | last seq |
| LedgerSeq   | the same as ledger_index |
| Status | |
| RawTxn | tx object stored as blob|
| TxnMeta | meta object stored as blob |  

RawTxn and TxnMeta are originally Hex. To retrieve them use
``` 
SELECT HEX(RawTxn), HEX(TxnMeta) from Transactions
```
To convert hex values to json string, use the binary decoders available here:
- (JS) https://github.com/ripple/ripple-binary-codec/blob/master/src/index.js#L28
- (GO) https://github.com/rubblelabs/ripple/tree/master/data, https://github.com/rubblelabs/ripple/blob/master/data/transaction.go
- (Java) https://github.com/sublimator/ripple-lib-java .Only ripple-bouncycastle and ripple-core are needed. 

This project is using Java decoder. The code is as follows:
```
String TransID = ""; // sourced from sqlitedb
       Long LedgerSeq = 0L; // sourced from sqlitedb
       byte[] RawTxn = new byte[]{}; // sourced from sqlitedb
       byte[] TxnMeta = new byte[]{}; // sourced from sqlitedb
       
       TransactionResult result = new TransactionResult(
               LedgerSeq,
               Hash256.fromHex(TransID),
               (Transaction) STObject.fromBytes(RawTxn) ,
               (TransactionMeta) STObject.fromBytes(TxnMeta)
       );
       
       result.toJSON();
```


**AccountTransactions**

| Name        | Description    |   
| -------------|:-------------: |
| TransId (PK) | transaction id hash |
| Account    | account that does or is affected by the transaction |
| LedgerSeq   | the same as ledger index |
| TxnSeq | transaction's sequence number unique for this account |

With the two tables, `account_tx` can be replicated by doing `JOIN ON TransId`. 

#### Ledger.db

`account_tx` only takes in ledger_index_min and ledger_index_max as parameters. 
This format is not practical when querying transactions between dates. 

To allow better query, Ledger.db is needed. 
One of the tables inside is Ledgers which contains timestamp. 

**Ledgers**
  

| Name        | Description    |   
| -------------|:-------------: |
| LedgerHash (PK) | transaction id hash |
| LedgerSeq    | ledger_index |
| ClosingTime   | timestamp in Ripple epoch|
| other columns |  |  


The only columns needed are LedgerSeq and ClosingTime. Closing time is in seconds of Ripple Epoch.

```Ripple epoch = UNIX seconds + 946684800```

Now it is possible to do `JOIN ON LedgerSeq` with the previous query. To do join with another database, use `ATTACH <db_path> AS alias`




  

 


