package it.unibo.agar.view

import it.unibo.agar.model.MockGameStateManager

import java.awt.Color
import java.awt.Graphics2D
import scala.swing.*

class GlobalView(manager: MockGameStateManager) extends MainFrame:

  title = "Agar.io - Global View"
  preferredSize = new Dimension(1000, 1000)

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val world = manager.getWorld
      AgarViewUtils.drawWorld(g, world)

  def showGameOver(message: String): Unit =
    Dialog.showMessage(
      contents.head,
      message,
      title = "Game Over",
      Dialog.Message.Info
    )
