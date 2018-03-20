package com.mbcu.mre.tx.exporter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.mbcu.mre.tx.exporter.actors.MainActor
import com.mbcu.mre.tx.exporter.utils.MyLoggingSingle
import scalikejdbc.config.DBs

object Application extends App {

  import akka.actor.Props


  override def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = akka.actor.ActorSystem("tx-dumper")
    implicit val materializer: ActorMaterializer = akka.stream.ActorMaterializer()

    val accPath = args(0)
    val sqlitePath = args(1)
    val logDir = args(2)
    MyLoggingSingle.init(logDir)
    DBs.setupAll()
    val mainActor = system.actorOf(Props(new MainActor(accPath, sqlitePath)), name = "main")
    mainActor ! "start"
  }


}
