package com.yadu.weather

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, RouteConcatenation}
import akka.stream.ActorMaterializer
import com.yadu.weather.consumers.FileDataConsumer
import com.yadu.weather.rest.TemperatureRest
import com.yadu.weather.service.{SnapshotPersistScheduler, TemperatureDataProcessor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Created by yadu on 31/05/20
 */

object MainApp extends RouteConcatenation with App with Directives {

  implicit val system = ActorSystem("WeatherAppActorSystem")
  implicit val mat    = ActorMaterializer()
  val contextRoot = "weatherApp"
  val server      = "localhost"
  val port        = 9000

  //create actors
  val temperatureDataProcessorActor = system.actorOf(Props[TemperatureDataProcessor])

  //initialize scheduler for snapshot save
  new SnapshotPersistScheduler(system, temperatureDataProcessorActor).scheduleSnapshotPersistence(10.seconds, 30.seconds)


  //initialize file processors
  val fileProcessor = new FileDataConsumer(temperatureDataProcessorActor)

  //schedule file processing in a fixed interval
  system.scheduler.schedule(10.seconds, 1.minute, new Runnable {
    override def run(): Unit = fileProcessor.readWeatherData()
  })

  val temperatureRest = new TemperatureRest(temperatureDataProcessorActor)

  val routes = pathPrefix(contextRoot){
    temperatureRest.temperatureRoute
  }

  Http().bindAndHandle(routes, server, port).map{ _ =>
    println(s"Successfully bound to http://$server:$port/$contextRoot")
  }.recover{
    case ex => ex.printStackTrace
  }

}
