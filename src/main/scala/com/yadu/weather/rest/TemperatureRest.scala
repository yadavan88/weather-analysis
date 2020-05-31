package com.yadu.weather.rest

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import akka.pattern._
import akka.util.Timeout
import com.yadu.weather.entities._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by yadu on 31/05/20
 */


class TemperatureRest(temperatureDataProcessor: ActorRef) extends Directives with JsonParsers {
  implicit val timeout = Timeout(3.seconds)
  val temperatureRoute = pathPrefix("temperature"){
    path("dailyMin"){
      post{
        entity(as[GetMinTempForDay]){ cmd =>
          complete(respond(cmd))
        }
      }
    } ~ path("dailyMax"){
      post{
        entity(as[GetMaxTempForDay]){ cmd =>
          complete(respond(cmd))
        }
      }
    } ~ path("weeklyMax"){
      post{
        entity(as[GetMaxTempForWeek]){ cmd =>
          complete(respond(cmd))
        }
      }
    } ~ path("weeklyMin"){
      post{
        entity(as[GetMinTempForWeek]){ cmd =>
          complete(respond(cmd))
        }
      }
    }
  }

  def respond(cmd: Any) = {
    val response = (temperatureDataProcessor ? cmd).mapTo[TemperatureResponse]
    response.map{ res =>
      HttpResponse(entity = HttpEntity(res.toJson.compactPrint))
    }.recover{
      case ex: NoSuchElementException => {
        HttpResponse(status = StatusCodes.NoContent)
      }
      case any                        => {
        HttpResponse(status = StatusCodes.InternalServerError, entity = HttpEntity(any.getMessage))
      }
    }
  }
}
