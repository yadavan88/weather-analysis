package com.yadu.weather.rest

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import com.yadu.weather.entities._
import spray.json.{DefaultJsonProtocol, _}

/**
 * Created by yadu on 31/05/20
 */


trait JsonParsers extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateJsonFormat extends RootJsonFormat[LocalDate] {
    override def write(obj: LocalDate) = JsString(obj.toString)

    override def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s, DateTimeFormatter.ISO_DATE) //parser.parseDateTime(s)
      case _           => throw new Exception("Malformed datetime")
    }
  }

  implicit val getMaxTempForDayFormat    = jsonFormat1(GetMaxTempForDay)
  implicit val getMinTempForDayFormat    = jsonFormat1(GetMinTempForDay)
  implicit val getMinTempForWeekFormat   = jsonFormat1(GetMinTempForWeek)
  implicit val getMaxTempForWeekFormat   = jsonFormat1(GetMaxTempForWeek)
  implicit val temperatureResponseFormat = jsonFormat1(TemperatureResponse)

  override implicit def sprayJsonMarshallerConverter[T](writer: RootJsonWriter[T])(implicit printer: JsonPrinter = CompactPrinter): ToEntityMarshaller[T] =
    sprayJsonMarshaller[T](writer, printer)
}
