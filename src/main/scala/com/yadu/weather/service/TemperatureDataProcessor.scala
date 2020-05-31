package com.yadu.weather.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.{Date, Locale}

import akka.actor.Actor
import akka.actor.Status.Failure
import akka.event.Logging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}
import com.yadu.weather.entities.IntervalType.{DAILY, WEEKLY}
import com.yadu.weather._
import com.yadu.weather.entities.{GetMaxTempForDay, GetMaxTempForWeek, GetMinTempForDay, GetMinTempForWeek, IntervalType, PersistSnapshotCommand, TemperatureDataCommand, TemperatureResponse, WeatherDataEvent, WeatherState}

import scala.collection.mutable

/**
 * Created by yadu on 30/05/20
 */


class TemperatureDataProcessor extends Actor with PersistentActor {
  val logger                                   = Logging(context.system, this)
  val state: mutable.Map[String, WeatherState] = mutable.Map.empty

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, _state: mutable.Map[String, WeatherState]) => {
      state ++= _state
    }
    case event: WeatherDataEvent                                     => {
      updateState(event)
    }
  }

  def queryHandlers: Receive = {
    case GetMaxTempForDay(date)  => {
      val key            = buildKey(date, DAILY)
      val requestedState = state.get(key)
      if (requestedState.isDefined)
        sender ! TemperatureResponse(requestedState.get.highestTemp)
      else
        sender ! Failure(new NoSuchElementException(s"No temperature data available for date : ${date}"))
    }
    case GetMinTempForDay(date)  => {
      val key            = buildKey(date, DAILY)
      val requestedState = state.get(key)
      if (requestedState.isDefined)
        sender ! TemperatureResponse(requestedState.get.lowestTemp)
      else
        sender ! Failure(new NoSuchElementException(s"No temperature data available for date : ${date}"))
    }
    case GetMaxTempForWeek(date) => {
      val key            = buildKey(date, WEEKLY)
      val requestedState = state.get(key)
      if (requestedState.isDefined)
        sender ! TemperatureResponse(requestedState.get.highestTemp)
      else
        sender ! Failure(new NoSuchElementException(s"No temperature data available for date : ${date}"))
    }
    case GetMinTempForWeek(date) => {
      val key            = buildKey(date, WEEKLY)
      val requestedState = state.get(key)
      if (requestedState.isDefined)
        sender ! TemperatureResponse(requestedState.get.lowestTemp)
      else
        sender ! Failure(new NoSuchElementException(s"No temperature data available for date : ${date}"))
    }
  }

  def snapshotHandlers: Receive = {
    case PersistSnapshotCommand                 => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata)          => logger.info("Snapshot saved for the persistenceId {} at {}", metadata.persistenceId, new Date(metadata.timestamp))
    case SaveSnapshotFailure(metadata, message) => logger.info("Snapshot saved for the persistenceId {} at {}, \nReason {}", metadata.persistenceId, new Date(metadata.timestamp), message)
  }

  def commandHandlers: Receive = {
    case cmd: TemperatureDataCommand => {
      println("Received weather data import command")
      persist(cmd.toWeatherDataEvent){ e =>
        updateState(e)
        //sender() ! e
      }
    }
  }

  override def receiveCommand: Receive = {
    commandHandlers.orElse(queryHandlers).orElse(snapshotHandlers)
  }

  def updateState(event: WeatherDataEvent) = {
    val date = event.timestamp.toLocalDate
    applyState(event, date, DAILY)
    applyState(event, date, WEEKLY)
    logger.info("Updated state successfully!")
  }

  private def applyState(event: WeatherDataEvent, date: LocalDate, intervalType: IntervalType) = {
    val key           = buildKey(date, intervalType)
    val existingState = state.get(key)
    if (existingState.nonEmpty) {
      val updatedState = existingState.get.applyTemp(event.temperature)
      state.put(key, updatedState)
    } else {
      val newState = WeatherState(event.temperature, event.temperature)
      state.put(key, newState)
    }
  }

  override def persistenceId: String = "temperature-data-processor"

  private def buildKey(date: LocalDate, intervalType: IntervalType): String = {
    intervalType match {
      case DAILY  => {
        val dateStr = date.format(DateTimeFormatter.BASIC_ISO_DATE)
        "DAILY_" + dateStr
      }
      case WEEKLY => {
        //Use Monday-Sunday week. This is not a fool proof approach. Will still go wrong across multiple years. Used as a temporary key fo week
        val weekNo = WeekFields.of(Locale.getDefault).weekOfWeekBasedYear();
        "WEEKLY_" + date.getYear + "_" + date.get(weekNo)
      }
    }
  }
}
