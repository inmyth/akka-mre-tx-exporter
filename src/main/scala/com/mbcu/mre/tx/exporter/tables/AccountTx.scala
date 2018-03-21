package com.mbcu.mre.tx.exporter.tables

import java.time.ZonedDateTime

import com.mbcu.mre.tx.exporter.utils.MyUtils
import scalikejdbc.{SQLSyntaxSupport, WrappedResultSet}

case class AccountTx(account : String, hash : String, ledgerIndex : Long, date : Long, humanDate : ZonedDateTime, txnType : String, txnAcc : String, tx : String, meta : String)
object AccountTx extends SQLSyntaxSupport[AccountTx] {

  override val tableName = "account_tx"

//  def apply(account : String, hash : String, ledgerIndex : Long, date : Long, txnType : String, txnAcc : String, tx : String, meta : String) =
//    AccountTx(account, hash, ledgerIndex, date, MyUtils.toHumanDate(date), txnType, txnAcc, tx, meta)

  def apply(rs: WrappedResultSet) : AccountTx = AccountTx(
    rs.string("account"),
    rs.string("hash"),
    rs.long("ledger_index"),
    rs.long("date"),
    rs.dateTime("human_date"),
    rs.string("transaction_type"),
    rs.string("txn_account"),
    rs.string("tx"),
    rs.string("meta")
  )
}

