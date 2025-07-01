package it.unibo.agar.view

import it.unibo.agar.model.{Coord, LocalGameStateManager, MockGameStateManager, World}

import java.awt.Graphics2D
import scala.swing.*

class LocalView(
                 manager: LocalGameStateManager,
                 playerId: String)
extends MainFrame:

  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)

  contents = new Panel:
    listenTo(keys, mouse.moves)
    focusable = true
    requestFocusInWindow()

    override def paintComponent(g: Graphics2D): Unit =
      val (offsetX, offsetY) = Some(manager.player)
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))

      val key = Coord(-1, -1) // TODO: fix this
      val seq = manager.getWorldCopy.players.getOrElse(key, Seq.empty)
      val drawWorld = World(
        width = manager.width,
        height = manager.height,
        players = manager.getWorldCopy.players.updated(key, seq ++ Seq(manager.player)), // TODO: fix this
        foods = manager.getWorldCopy.foods
      )
      AgarViewUtils.drawWorld(g, drawWorld, offsetX, offsetY)

    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      val dx = (mousePos.x - size.width / 2) * 0.01
      val dy = (mousePos.y - size.height / 2) * 0.01
      manager.movePlayerDirection(dx, dy)
      repaint()
    }
  
  def showGameOver(message: String): Unit =
    Dialog.showMessage(
      contents.head,
      message,
      title = "Game Over",
      Dialog.Message.Info
    )

object LocalView:
  def empty: LocalView =
    LocalView(null, "")
