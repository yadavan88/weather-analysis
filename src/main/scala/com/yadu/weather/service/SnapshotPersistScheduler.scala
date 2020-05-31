package com.yadu.weather.service

import akka.actor.{ActorRef, ActorSystem}
import com.yadu.weather.entities.PersistSnapshotCommand

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by yadu on 31/05/20
 */


class SnapshotPersistScheduler(system: ActorSystem, temperatureDataProcessor: ActorRef) {

  def scheduleSnapshotPersistence(init:FiniteDuration, interval:FiniteDuration) = {
    system.scheduler.schedule(init, interval, temperatureDataProcessor, PersistSnapshotCommand)
  }
}
