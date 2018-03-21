package com.mbcu.mre.tx.exporter.actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import akka.dispatch.ExecutionContexts.global
import com.mbcu.mre.tx.exporter.actors.DbActor.{AccountTxSaved, SaveAccountTx}
import com.mbcu.mre.tx.exporter.actors.MainActor.{LogRemainder, Shutdown}
import com.mbcu.mre.tx.exporter.actors.SqliteActor.{GotAccTx, SelectAccTx, SqliteReady, Start}
import com.mbcu.mre.tx.exporter.utils.MyLogging
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.Duration
import scala.concurrent.duration._


object MainActor {
  def props(accsPath : String, sqlitePath : String): Props = Props(new MainActor(accsPath, sqlitePath))

  object LogRemainder

  case class Shutdown(code : Int)
}

class MainActor(accsPath : String, sqlitePath : String) extends Actor with MyLogging {
  private var cancellable : Option[Cancellable] = None
  var accounts : ListBuffer[String] = ListBuffer.empty
  var dbActor : Option[ActorRef] = None
  var sqliteActor : Option[ActorRef] = None
  implicit val ec: ExecutionContextExecutor = global

  override def receive: Receive = {

    case "start" =>
      import scala.io.Source._

      accounts ++= fromFile(accsPath).getLines().toSeq
      setupScheduleLog()
      val sqlite = context.actorOf(Props(new SqliteActor(sqlitePath)), name = "sqlite")
      sqliteActor = Some(sqlite)
      val db = context.actorOf(Props(new DbActor()), name = "db")
      dbActor = Some(db)
      db ! "start"

    case "sql db ready" =>  sqliteActor foreach(_ ! "start")

    case SqliteReady =>
      info("Connection to SQLite has been established.")
      self ! "start from head"

    case "start from head" =>
      accounts match {
        case a if a.nonEmpty =>
          val acc = accounts.head
          info(s"""Processing $acc""")
          sqliteActor foreach( _ ! SelectAccTx(acc, 0))
        case _ => self ! Shutdown(0)
      }

    case GotAccTx(acc, offset, accTxs) => dbActor foreach(_ ! SaveAccountTx(acc, offset, accTxs))

    case AccountTxSaved(acc, offset) =>
      offset match {
        case Some(o) => sqliteActor foreach( _ ! SelectAccTx(acc, o))
        case _ =>
          info(s"""Done processing $acc""")
          accounts.remove(0)
          self ! "start from head"
      }

    case LogRemainder =>
      val s = accounts mkString "\n"
      info(
        s"""
           |START
           |$s
           |END
         """.stripMargin)

    case Shutdown(code) =>
      sqliteActor foreach (_ ! "terminate")
      info(s"Stopping application, code $code")
      implicit val executionContext: ExecutionContext = context.system.dispatcher
      context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(code))
  }

  def setupScheduleLog() : Unit = {
    val scheduleActor = context.actorOf(Props(classOf[ScheduleActor]))
    cancellable =Some(
      context.system.scheduler.schedule(
        10 second,
        600 second,
        scheduleActor,
        LogRemainder))
  }

}
