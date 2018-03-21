package com.mbcu.mre.tx.exporter.actors

import akka.actor.{Actor, ActorRef}
import com.mbcu.mre.tx.exporter.actors.DbActor.{AccountTxSaved, SaveAccountTx}
import com.mbcu.mre.tx.exporter.tables.AccountTx
import com.mbcu.mre.tx.exporter.utils.MyLogging
import scalikejdbc.AutoSession
import scalikejdbc._

object DbActor {
  case class SaveAccountTx(acc: String, offset : Option[Int], data : Seq[AccountTx])

  case class AccountTxSaved(account : String, offset : Option[Int])


}
class DbActor extends Actor with MyLogging{
  implicit val session: AutoSession.type = AutoSession
  var mainActor : Option[ActorRef] = None

  override def receive: Receive = {
    case "start" =>
      mainActor = Some(sender())
      mainActor foreach(_ ! "sql db ready")

    case SaveAccountTx(acc, offset, data) =>
      val params = data.map(a => Seq(a.account, a.hash, a.ledgerIndex, a.date, a.humanDate, a.txnType, a.txnAcc, a.tx, a.meta))

     sql"""insert into main.account_tx (account, hash, ledger_index, date, human_date, transaction_type, txn_account, tx, meta)
           values (?,?,?,?,?,?,?,?,?)
           ON DUPLICATE KEY UPDATE hash = hash"""
      .batch(params:_*)
      .apply()
      mainActor foreach(_ ! AccountTxSaved(acc, offset))

    case "select all" =>
      val accounts: List[AccountTx] = sql"select * from account_tx".map(rs => AccountTx(rs)).list.apply()
      accounts.foreach(a => info(a.toString))
  }


}
