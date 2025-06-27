package it.unibo.agar

import akka.actor.typed.ActorSystem
import it.unibo.agar.actor.{PlayerActor, ZoneManager}
import it.unibo.agar.model.{AIMovement, GameInitializer, MockGameStateManager, World}
import it.unibo.agar.view.{GlobalView, LocalView}

import java.awt.Window
import java.util.{Timer, TimerTask}
import scala.swing.Swing.onEDT
import scala.swing.{Frame, SimpleSwingApplication}

object MainTest:
  def main(args: Array[String]): Unit = {
    println("Agar game server is starting...")

    val system = ActorSystem(ZoneManager(1000, 1000, 100), "AgarGameSystem")

    val player = system.systemActorOf(
      PlayerActor("player1"),
      "Player1"
    )

  }

object Main extends SimpleSwingApplication:

  private val width = 1000
  private val height = 1000
  private val manager = ActorSystem(ZoneManager(1000, 1000, 100), "AgarGameSystem")

//  private val timer = new Timer()
//  private val task: TimerTask = new TimerTask:
//    override def run(): Unit =
//      AIMovement.moveAI("p1", manager)
//      manager.tick()
//      onEDT(Window.getWindows.foreach(_.repaint()))
//  timer.scheduleAtFixedRate(task, 0, 30) // every 30ms

  override def top: Frame =
    // Open both views at startup
    //new LocalView(manager, "p1").open()
    // No launcher window, just return an empty frame (or null if allowed)
    new Frame { visible = false }
