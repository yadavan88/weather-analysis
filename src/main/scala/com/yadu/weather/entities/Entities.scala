package com.yadu.weather.entities

import java.time.{LocalDate, LocalDateTime}

/**
 * Created by yadu on 30/05/20
 */


case class TemperatureDataCommand(timestamp: LocalDateTime, temperature: Double) {
  def toWeatherDataEvent = WeatherDataEvent(timestamp, temperature)
}

case class WeatherDataEvent(timestamp: LocalDateTime, temperature: Double)

case class WeatherState(highestTemp: Double, lowestTemp: Double) {
  def applyTemp(temp: Double): WeatherState = {
    if (temp < lowestTemp) {
      this.copy(lowestTemp = temp)
    } else if (temp > highestTemp) {
      this.copy(highestTemp = temp)
    } else this
  }
}

case class TemperatureResponse(temp:Double)

case class GetMaxTempForDay(date: LocalDate)

case class GetMinTempForDay(date: LocalDate)

case class GetMaxTempForWeek(date: LocalDate)

case class GetMinTempForWeek(date: LocalDate)

case object PersistSnapshotCommand

sealed trait IntervalType

object IntervalType {
  case object DAILY extends IntervalType
  case object WEEKLY extends IntervalType
}