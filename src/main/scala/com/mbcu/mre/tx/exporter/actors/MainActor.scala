package com.mbcu.mre.tx.exporter.actors

import akka.actor.{Actor, ActorRef, Props}
import com.mbcu.mre.tx.exporter.actors.MainActor.Shutdown
import com.mbcu.mre.tx.exporter.actors.SqliteActor.SqliteReady
import com.mbcu.mre.tx.exporter.utils.MyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object MainActor {

  def props(accsPath : String, sqlitePath : String): Props = Props(new MainActor(accsPath, sqlitePath))

  case class Shutdown(code : Int)
}

class MainActor(accsPath : String, sqlitePath : String) extends Actor with MyLogging {
  var sqliteActor : Option[ActorRef] = None

  override def receive: Receive = {

    case "start" =>
      val sqlite = context.actorOf(Props(new SqliteActor(sqlitePath)), name = "sqlite")
      sqliteActor = Some(sqlite)
      sqlite ! "start"

    case SqliteReady =>
      info("Connection to SQLite has been established.")
      sqliteActor foreach(_ ! "test")


    case Shutdown(code) =>
      info(s"Stopping application, code $code")
      implicit val executionContext: ExecutionContext = context.system.dispatcher
      context.system.scheduler.scheduleOnce(Duration.Zero)(System.exit(code))
  }



}
