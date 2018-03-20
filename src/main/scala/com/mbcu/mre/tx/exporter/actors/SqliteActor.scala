package com.mbcu.mre.tx.exporter.actors

import java.sql.{Connection, Statement}

import akka.actor.{Actor, ActorRef, Props}
import com.mbcu.mre.tx.exporter.actors.SqliteActor.SqliteReady
import com.ripple.core.coretypes.STObject
import com.ripple.core.coretypes.hash.Hash256
import com.ripple.core.types.known.tx.Transaction
import com.ripple.core.types.known.tx.result.{TransactionMeta, TransactionResult}

object SqliteActor {
  def props(sqlitePath : String): Props = Props(new SqliteActor(sqlitePath))

  object SqliteReady

}

class SqliteActor(sqlitePath : String) extends Actor {
  var mainActor : Option[ActorRef] = None
  var conn : Option[Connection] = None
  val url = s"jdbc:sqlite:$sqlitePath"

  override def receive: Receive = {

    case "start" =>
      mainActor = Some(sender())
      import java.sql.DriverManager
      import java.sql.SQLException
      try {
        conn = Some(DriverManager.getConnection(url))
        mainActor foreach(_ ! SqliteReady)
      } catch {
        case e: SQLException => println(e.getMessage)
      }
//      finally try
//          if (conn != null) conn.foreach(_.close())
//      catch {
//        case ex: SQLException => println(ex.getMessage)
//      }



    case "test" =>
      //TransID|TransType|FromAcct|FromSeq|LedgerSeq|Status|RawTxn|TxnMeta
      // CEAF976B2BF92F2C51C4D8AE34085CFC3E435A23690F3306B6BD517773C91931|TrustSet|rRXPAamu7uBESCfQBvNxSVNJvKSqbsmGB|13|3713038|V||
      val q = " SELECT TransId, TransType, FromAcct, FromSeq, LedgerSeq, Status, HEX(RawTxn) as RawTxn, HEX(TxnMeta) as TxnMeta" +
        " FROM Transactions ORDER BY LedgerSeq DESC limit 1;"
      conn match {
        case Some(c) =>
          val rs = c.createStatement().executeQuery(q)
          new Iterator[(String, String)] {
            def hasNext = rs.next()
//            def next() = (rs.getString(1), rs.getString(2))

            def next() : (String, String) = {
              val transId = rs.getString(1)
              val transType = rs.getString(2)
              val fromAcct = rs.getString(3)
              val fromSeq = rs.getInt(4)
              val ledgerSeq = rs.getLong(5)
              val status = rs.getInt(6)
              val rawTxn = rs.getString(7)
              val txnMeta = rs.getString(8)
//              Transaction(transId, transType, fromAcct, fromSeq, ledgerSeq, status, rawTxn, txnMeta)
//              val tx = new TransactionResult(
//                ledgerSeq,
//                Hash256.fromHex(transId),
//                STObject.fromHex(rawTxn).asInstanceOf[Transaction],
//                STObject.fromHex(txnMeta).asInstanceOf[TransactionMeta]
//
//              )

              val txn = STObject.fromHex(rawTxn).asInstanceOf[Transaction].prettyJSON()
              val meta = STObject.fromHex(txnMeta).asInstanceOf[TransactionMeta].prettyJSON()

              (txn, meta )
            }
          }.toStream
            .foreach(println)
          rs.close()

        case _ =>
      }

  }

}
