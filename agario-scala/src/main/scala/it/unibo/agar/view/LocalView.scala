package it.unibo.agar.view

import it.unibo.agar.model.{Player, ViewWorld}

import java.awt.Graphics2D
import scala.swing.*

class LocalView(playerId: String, movementReaction: (Double, Double) => Unit) extends MainFrame:

  var viewWorld: ViewWorld = ViewWorld.empty // Initialize with an empty world

  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)

  contents = new Panel:
    listenTo(keys, mouse.moves)
    focusable = true
    requestFocusInWindow()

    override def paintComponent(g: Graphics2D): Unit =
      val world = viewWorld
      val playerOpt = world.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))
      AgarViewUtils.drawWorld(g, world, offsetX, offsetY)

    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      val playerOpt = viewWorld.players.find(_.id == playerId)
      playerOpt.foreach: player =>
        val dx = (mousePos.x - size.width / 2) * 0.01
        val dy = (mousePos.y - size.height / 2) * 0.01
        movementReaction(dx, dy)
      repaint()
    }

  def updateWorld(newWorld: ViewWorld): Unit =
    // Update the view world with the new state
    this.viewWorld = this.viewWorld.appendPlayers(newWorld.players) // TODO: ci vuole una copia? va usato EDT?
    this.viewWorld = this.viewWorld.appendFood(newWorld.foods)
    println(viewWorld.foods)
    repaint()

  def updatePlayer(player: Player): Unit =
    // Show a specific player in the view
    this.viewWorld = this.viewWorld.updatePlayer(player)
    repaint()


