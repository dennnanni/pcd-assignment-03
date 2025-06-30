package it.unibo.agar.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorSystem
import it.unibo.agar.actor.{PlayerActor, ZoneActor}
import it.unibo.agar.model.{Coord, WorldGrid}

import java.util.{Timer, TimerTask}
import scala.swing.*
import scala.util.Random

object Main extends SimpleSwingApplication:

  private val width = 1000
  private val height = 1000

  private val system = ActorSystem(Behaviors.empty, "AgarGameSystem")

  private val grid = WorldGrid(width, height, 400)
  private val allZones = grid.allCoords.map(
    coord =>
      val (minW, maxW, minH, maxH) = grid.boundsOf(coord)
      val zoneRef = system.systemActorOf(
        ZoneActor(minW, maxW, minH, maxH, coord),
        s"Zone-${coord.x}-${coord.y}"
      )
      coord -> zoneRef
  ).toMap

  private val timer = new Timer()
  private val task: TimerTask = new TimerTask:
    override def run(): Unit =
      val (x, y): (Double, Double) = (Random.between(0, width).toDouble, Random.between(0, height).toDouble)
      val coord = grid.coordFor(x, y)
      allZones.get(coord).foreach { zoneRef =>
        zoneRef ! ZoneActor.AddFood(x, y)
      }
  timer.scheduleAtFixedRate(task, 0, 500) // every 30ms

  private val playerActor = system.systemActorOf(
    PlayerActor("p1"),
    "Player1"
  )

  override def top: Frame = {
    // Open both views at startup
    playerActor ! PlayerActor.Init(width, height, allZones)
    // No launcher window, just return an empty frame (or null if allowed)
    new Frame { visible = false }
  }
