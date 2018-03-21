package com.mbcu.mre.tx.exporter.actors

import akka.actor.Actor
import com.mbcu.mre.tx.exporter.actors.MainActor.LogRemainder


class ScheduleActor extends Actor {

  override def receive: Receive = {

    case LogRemainder =>
      sender() ! LogRemainder
  }

}