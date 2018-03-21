package com.mbcu.mre.tx.exporter.actors

import java.sql.Connection
import akka.actor.{Actor, ActorRef, Props}
import com.mbcu.mre.tx.exporter.actors.MainActor.Shutdown
import com.mbcu.mre.tx.exporter.actors.SqliteActor.{GotAccTx, SelectAccTx, SqliteReady, Start}
import com.mbcu.mre.tx.exporter.tables.AccountTx
import com.mbcu.mre.tx.exporter.utils.{MyLogging, MyUtils}
import com.ripple.core.coretypes.STObject
import com.ripple.core.coretypes.hash.Hash256
import com.ripple.core.types.known.tx.Transaction
import com.ripple.core.types.known.tx.result.{TransactionMeta, TransactionResult}
import com.typesafe.config.ConfigFactory

object SqliteActor {
  def props(dbDir : String): Props = Props(new SqliteActor(dbDir))

  case class Start(limit : Int)

  object SqliteReady

  case class SelectAccTx(acc : String, offset : Int)

  case class GotAccTx(acc: String, newOffset : Option[Int], res : Seq[AccountTx] )

}

class SqliteActor(dbDir : String) extends Actor with MyLogging {
  val limit : Int = ConfigFactory.load().getInt("sqlite.limit")
  val maxLedger : Long = ConfigFactory.load().getLong("sqlite.maxLedger")
  val minLedger : Long = ConfigFactory.load().getLong("sqlite.minLedger")

  var mainActor : Option[ActorRef] = None
  var conn : Option[Connection] = None
  val transactionPath: String = dbDir + "transaction.db"
  val ledgerPath : String = dbDir + "ledger.db"
  val url = s"jdbc:sqlite:$transactionPath"

  override def receive: Receive = {

    case "start" =>
      mainActor = Some(sender())
      import java.sql.DriverManager
      import java.sql.SQLException
      try {
        val c = DriverManager.getConnection(url)
        val st = c.createStatement()
        st.execute(s"ATTACH DATABASE '$ledgerPath' AS ledger")
        conn = Some(c)
        mainActor foreach(_ ! SqliteReady)
      } catch {
        case e: SQLException =>
          info(e.getMessage)
          mainActor foreach(_ ! Shutdown(-1))
      }
//      finally try
//          if (conn != null) conn.foreach(_.close())
//      catch {
//        case ex: SQLException => println(ex.getMessage)
//      }

    case SelectAccTx(acc, offset) =>
      val q = s"""SELECT Transactions.TransID as TransId, Transactions.LedgerSeq as LedgerSeq, Transactions.FromAcct as FromAcct,
                ClosingTime, Transactions.TransType as TransType,
                HEX(Transactions.RawTxn) as RawTxn, HEX(Transactions.TxnMeta) as TxnMeta FROM AccountTransactions
                JOIN Transactions ON AccountTransactions.TransID = Transactions.TransID
                JOIN ledger.Ledgers as ledger ON ledger.LedgerSeq = Transactions.LedgerSeq
                WHERE AccountTransactions.Account = '$acc'
                AND Transactions.LedgerSeq BETWEEN $minLedger AND $maxLedger
                ORDER BY Transactions.LedgerSeq ASC LIMIT $limit OFFSET $offset"""
      conn match {
        case Some(c) =>
          val rs = c.createStatement().executeQuery(q)
          val res  = new Iterator[AccountTx] {
            def hasNext: Boolean = rs.next()

            def next() : (AccountTx) = {
              val transId = rs.getString(1)
              val ledgerSeq = rs.getLong(2)
              val fromAcct = rs.getString(3)
              val closingTime = rs.getLong(4)
              val transType = rs.getString(5)
              val rawTxn = rs.getString(6)
              val txnMeta = rs.getString(7)
              val txn = STObject.fromHex(rawTxn).asInstanceOf[Transaction]
              val meta = STObject.fromHex(txnMeta).asInstanceOf[TransactionMeta]
              val t = new TransactionResult(
                ledgerSeq,
                Hash256.fromHex(transId),
                txn,
                meta
              )
              AccountTx(acc, transId, t.ledgerIndex.value(), closingTime, MyUtils.toHumanDate(closingTime), transType, fromAcct, t.txn.prettyJSON(), t.meta.prettyJSON())
            }
          }.toList

          rs.close()
          val newOffset = if (res.size == limit) Some(offset + limit) else None
          mainActor foreach(_ ! GotAccTx(acc, newOffset, res))

        case _ =>
          error("SQLiteActor : no connection")
          mainActor foreach(_ ! Shutdown(-1))
      }


//    case "test" =>
//      //TransID|TransType|FromAcct|FromSeq|LedgerSeq|Status|RawTxn|TxnMeta
//      // CEAF976B2BF92F2C51C4D8AE34085CFC3E435A23690F3306B6BD517773C91931|TrustSet|rRXPAamu7uBESCfQBvNxSVNJvKSqbsmGB|13|3713038|V||
//      val acc = "r3FECuTv723qHV5iEPjGzafi7onwxZA5su"
//      val q = s"""SELECT Transactions.TransID as TransId, Transactions.LedgerSeq as LedgerSeq, Transactions.FromAcct as FromAcct,
//                Transactions.ClosingTime, Transaction.TransType as TransType,
//                HEX(Transactions.RawTxn) as RawTxn, HEX(Transactions.TxnMeta) as TxnMeta FROM AccountTransactions
//                JOIN Transactions ON AccountTransactions.TransID = Transactions.TransID
//                JOIN ledger.Ledgers as ledger ON ledger.LedgerSeq = Transactions.LedgerSeq
//                WHERE AccountTransactions.Account = '$acc'
//                ORDER BY Transactions.LedgerSeq ASC LIMIT 1"""
//      conn match {
//        case Some(c) =>
//          val rs = c.createStatement().executeQuery(q)
//          new Iterator[AccountTx] {
//            def hasNext = rs.next()
////            def next() = (rs.getString(1), rs.getString(2))
//
//            def next() : (AccountTx) = {
//              val transId = rs.getString(1)
//              val ledgerSeq = rs.getLong(2)
//              val fromAcct = rs.getString(3)
//              val closingTime = rs.getLong(4)
//              val transType = rs.getString(5)
//              val rawTxn = rs.getString(6)
//              val txnMeta = rs.getString(7)
//              val txn = STObject.fromHex(rawTxn).asInstanceOf[Transaction]
//              val meta = STObject.fromHex(txnMeta).asInstanceOf[TransactionMeta]
//              val t = new TransactionResult(
//                ledgerSeq,
//                Hash256.fromHex(transId),
//                txn,
//                meta
//              )
//              AccountTx(acc, transId, t.ledgerIndex.value(), closingTime, MyUtils.toHumanDate(closingTime), transType, fromAcct, t.txn.prettyJSON(), t.meta.prettyJSON())
//            }
//          }.toSeq
//
//          rs.close()
//
//        case _ =>
//          error("SQLiteActor : no connection")
//          mainActor foreach(_ ! Shutdown(-1))
//      }

    case "terminate" => conn foreach(_.close())
  }

}
