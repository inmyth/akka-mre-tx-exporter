package com.mbcu.mre.tx.exporter.utils

import java.time.format.DateTimeFormatterBuilder
import java.time.{Instant, ZoneId, ZonedDateTime}

object MyUtils {
  val formatter = new DateTimeFormatterBuilder()


  def toZonedDateTime(in : String) : ZonedDateTime = {
    ZonedDateTime.parse(in.toCharArray)
  }


  def toLinuxEpoch(rippleEpoch : Long): Long = rippleEpoch + 946684800


  def toHumanDate(rippleEpoch : Long) : ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(toLinuxEpoch(rippleEpoch)), ZoneId.of("Z"))

}

