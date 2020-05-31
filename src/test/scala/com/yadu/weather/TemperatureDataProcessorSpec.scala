package com.yadu.weather

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.Status.Failure
import akka.actor.{ActorSystem, Props, Status}
import akka.testkit.{ImplicitSender, TestKit}
import com.yadu.weather.entities.{GetMaxTempForDay, GetMaxTempForWeek, GetMinTempForDay, GetMinTempForWeek, TemperatureDataCommand, TemperatureResponse}
import com.yadu.weather.service.{SnapshotPersistScheduler, TemperatureDataProcessor}
import org.scalatest.WordSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

/**
 * Created by yadu on 30/05/20
 */


class TemperatureDataProcessorSpec extends TestKit(ActorSystem("Test")) with WordSpecLike with Matchers with ImplicitSender {

  "Temperature processor spec" should {
    "process command and persist even when a new command is sent" in {
      val weatherActor = system.actorOf(Props[TemperatureDataProcessor])
      new SnapshotPersistScheduler(system, weatherActor).scheduleSnapshotPersistence(500.millis, 1.seconds)

      val timestamp1 = LocalDateTime.parse("2020-05-25T14:03:23Z", DateTimeFormatter.ISO_DATE_TIME)
      val cmd1       = TemperatureDataCommand(timestamp1, 24.2345D)
      weatherActor ! cmd1

      val getMaxTempForDay1  = GetMaxTempForDay(timestamp1.toLocalDate)
      val getMaxTempForWeek1 = GetMaxTempForWeek(timestamp1.toLocalDate)
      val getMinTempForDay1  = GetMinTempForDay(timestamp1.toLocalDate)
      val getMinTempForWeek1 = GetMinTempForWeek(timestamp1.toLocalDate)

      //Min and max for the day and week should be same, since there is only one event received so far
      weatherActor ! getMaxTempForDay1
      expectMsg(TemperatureResponse(cmd1.temperature))
      weatherActor ! getMaxTempForWeek1
      expectMsg(TemperatureResponse(cmd1.temperature))
      weatherActor ! getMinTempForDay1
      expectMsg(TemperatureResponse(cmd1.temperature))
      weatherActor ! getMinTempForWeek1
      expectMsg(TemperatureResponse(cmd1.temperature))

      //Get max when the there is no state for the input date
      val wrongMaxDayCmd = GetMaxTempForDay(timestamp1.minusDays(10).toLocalDate)
      weatherActor ! wrongMaxDayCmd
      expectMsgType[Status.Failure]
      val wrongMaxWeekCmd = GetMaxTempForWeek(timestamp1.minusDays(8).toLocalDate)
      weatherActor ! wrongMaxWeekCmd
      expectMsgType[Status.Failure]

      //get updated max and min when more events are raised
      val cmd2 = TemperatureDataCommand(timestamp1.plusDays(2), 32D)
      weatherActor ! cmd2
      val cmd3 = TemperatureDataCommand(timestamp1.plusDays(2), 25D)
      weatherActor ! cmd3
      //query for day
      val getMaxDay2 = GetMaxTempForDay(cmd2.timestamp.toLocalDate)
      weatherActor ! getMaxDay2
      expectMsg(TemperatureResponse(cmd2.temperature))
      val getMinDay2 = GetMinTempForDay(cmd2.timestamp.toLocalDate)
      weatherActor ! getMinDay2
      expectMsg(TemperatureResponse(cmd3.temperature))
      //query for week
      val getMinWeek2 = GetMinTempForWeek(cmd2.timestamp.toLocalDate)
      weatherActor ! getMinWeek2
      expectMsg(TemperatureResponse(cmd1.temperature))
      val getMaxWeek2 = GetMaxTempForWeek(cmd2.timestamp.toLocalDate)
      weatherActor ! getMaxWeek2
      expectMsg(TemperatureResponse(cmd2.temperature))

      val cmd4 = TemperatureDataCommand(timestamp1.plusDays(4), 17.32D)
      weatherActor ! cmd4
      val getMinWeek2Again = GetMinTempForWeek(cmd4.timestamp.toLocalDate)
      weatherActor ! getMinWeek2Again
      expectMsg(TemperatureResponse(cmd4.temperature))
      val getMaxWeek2Again = GetMaxTempForWeek(cmd4.timestamp.toLocalDate)
      weatherActor ! getMaxWeek2Again
      expectMsg(TemperatureResponse(cmd2.temperature))

      //send an event for another week
      val cmdForOldWeek = TemperatureDataCommand(timestamp1.minusDays(10), 9.32D)
      weatherActor ! cmdForOldWeek

      val getMinForOldWeek = GetMinTempForWeek(cmdForOldWeek.timestamp.toLocalDate)
      weatherActor ! getMinForOldWeek
      expectMsg(TemperatureResponse(cmdForOldWeek.temperature))

      val getMaxForOldWeek = GetMaxTempForWeek(cmdForOldWeek.timestamp.toLocalDate)
      weatherActor ! getMaxForOldWeek
      expectMsg(TemperatureResponse(cmdForOldWeek.temperature))

      val getMaxForOldDay = GetMaxTempForDay(cmdForOldWeek.timestamp.toLocalDate)
      weatherActor ! getMaxForOldDay
      expectMsg(TemperatureResponse(cmdForOldWeek.temperature))

      val getMinForOldDay = GetMinTempForDay(cmdForOldWeek.timestamp.toLocalDate)
      weatherActor ! getMinForOldDay
      expectMsg(TemperatureResponse(cmdForOldWeek.temperature))

    }
  }

}
