package com.yadu.weather.rest

import java.time.LocalDate

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.yadu.weather.entities._
import com.yadu.weather.service.TemperatureDataProcessor
import org.scalatest.WordSpec
import org.scalatest.matchers.should.Matchers
import spray.json._

/**
 * Created by yadu on 31/05/20
 */


class TemperatureRestSpec extends WordSpec with Matchers with ScalatestRouteTest with JsonParsers {

  "Temperature Rest Spec" should {
    implicit val system = ActorSystem("TestSystem")
    val temperatureActor = system.actorOf(Props[TemperatureDataProcessor])
    //init the actor with some data for the test
    val date             = LocalDate.parse("2020-05-29")

    temperatureActor ! TemperatureDataCommand(date.atTime(10, 45), 29.3D)
    temperatureActor ! TemperatureDataCommand(date.atTime(10, 32), 17.25D)

    val temperatureRoute = new TemperatureRest(temperatureActor).temperatureRoute

    "get max/min temperature for the given date and week" in {

      Post("/temperature/dailyMin", GetMinTempForDay(date)) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.OK
        val tempResponse = responseAs[String].parseJson.convertTo[TemperatureResponse]
        tempResponse shouldBe TemperatureResponse(17.25D)
      }

      Post("/temperature/dailyMax", GetMaxTempForDay(date)) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.OK
        val tempResponse = responseAs[String].parseJson.convertTo[TemperatureResponse]
        tempResponse shouldBe TemperatureResponse(29.3D)
      }

      Post("/temperature/weeklyMax", GetMaxTempForWeek(date)) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.OK
        val tempResponse = responseAs[String].parseJson.convertTo[TemperatureResponse]
        tempResponse shouldBe TemperatureResponse(29.3D)
      }

      Post("/temperature/weeklyMin", GetMinTempForWeek(date)) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.OK
        val tempResponse = responseAs[String].parseJson.convertTo[TemperatureResponse]
        tempResponse shouldBe TemperatureResponse(17.25D)
      }

      Post("/temperature/weeklyMin", GetMinTempForWeek(date.plusDays(10))) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.NoContent
      }

      Post("/temperature/dailyMin", GetMinTempForDay(date.plusDays(10))) ~> temperatureRoute ~> check{
        status shouldBe StatusCodes.NoContent
      }
    }
  }

}
