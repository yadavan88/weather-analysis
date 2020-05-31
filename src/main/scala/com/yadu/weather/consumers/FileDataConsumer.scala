package com.yadu.weather.consumers

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorRef
import com.yadu.weather.entities.TemperatureDataCommand

import scala.io.Source
import scala.util.Try

/**
 * Created by yadu on 31/05/20
 */
class FileDataConsumer(temperatureDataProcessor: ActorRef) {

  val WEATHER_DATA_DIR = "dataDir"

  def readWeatherData() = {
    val tried = Try{
      val files = new File(WEATHER_DATA_DIR).listFiles(f => f.isFile && f.getName.endsWith(".csv")).toList
      if (files.nonEmpty) {
        val file     = files.head
        val commands = Source.fromFile(file).getLines().drop(10).map{ line =>
          val splittedLine = line.split(",")
          val timestamp    = LocalDateTime.parse(splittedLine.head, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"))
          val temp         = splittedLine.last.toDouble
          TemperatureDataCommand(timestamp, temp)
        }.toList
        println(s"Publishing ${commands.size} messages to process")
        commands.foreach(c => temperatureDataProcessor ! c)
        //move the file to archive directory
        createArchiveAndMoveFile(files.head)
        println("Processed weather file successfully")
      } else {
        println(s"No files to process from ${WEATHER_DATA_DIR}.")
      }
    }
    if (tried.isFailure) {
      println("Failed during file processing. " + tried.failed.get.getMessage)
      tried.failed.get.printStackTrace()
    }
    tried
  }

  private def createArchiveAndMoveFile(file: File) = {
    val archiveDir = s"${WEATHER_DATA_DIR}/archives/"
    val status     = Try{
      Files.createDirectories(Paths.get(archiveDir))
      Files.move(Paths.get(file.toURI), Paths.get(archiveDir + file.getName))
    }
    if (status.isFailure) {
      println(s"Error while moving the file [${file.getPath}] to archives directory [${archiveDir}]")
    }
  }

}
