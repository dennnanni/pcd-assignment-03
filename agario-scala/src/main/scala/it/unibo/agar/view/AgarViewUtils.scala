package it.unibo.agar.view

import it.unibo.agar.model.World

import java.awt.Color
import java.awt.Graphics2D

object AgarViewUtils:

  private val playerBorderColor = Color.black
  private val playerLabelOffsetX = 10
  private val playerLabelOffsetY = 0
  private val playerInnerOffset = 2
  private val playerInnerBorder = 4
  private val playerPalette: Array[Color] =
    Array(Color.blue, Color.orange, Color.cyan, Color.pink, Color.yellow, Color.red, Color.green, Color.lightGray)

  private def playerColor(id: String): Color = id match
    case pid if pid.startsWith("p") =>
      val idx = pid.drop(1).toIntOption.getOrElse(0)
      playerPalette(idx % playerPalette.length)
    case _ => Color.gray

  def drawWorld(
      g: Graphics2D,
      world: World,
      offsetX: Double = 0,
      offsetY: Double = 0
  ): Unit =
    def toScreenCenter(x: Double, y: Double, radius: Int): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)

    def toScreenLabel(x: Double, y: Double): (Int, Int) =
      ((x - offsetX - playerLabelOffsetX).toInt, (y - offsetY - playerLabelOffsetY).toInt)

    val lines = Seq(0, 400, 800, 1000, 1200)
    g.setColor(Color.LIGHT_GRAY)

    def worldX(x: Int): Int = x - offsetX.toInt
    def worldY(y: Int): Int = y - offsetY.toInt

    // Vertical lines
    for (x <- lines) {
      val sx = worldX(x)
      if (sx >= 0 && sx <= world.width)
        g.drawLine(sx, 0, sx, world.height.toInt)
    }
    // Horizontal lines
    for (y <- lines) {
      val sy = worldY(y)
      if (sy >= 0 && sy <= world.height)
        g.drawLine(0, sy, world.width.toInt, sy)
    }

    // Draw foods
    g.setColor(Color.green)
    world.foods.flatten(f => f._2).foreach: food =>
      val radius = food.radius.toInt
      val diameter = radius * 2
      val (foodX, foodY) = toScreenCenter(food.x, food.y, radius)
      g.fillOval(foodX, foodY, diameter, diameter)

    // Draw players
    world.players.flatten(p => p._2).foreach: player =>
      val radius = player.radius.toInt
      val diameter = radius * 2
      val (borderX, borderY) = toScreenCenter(player.x, player.y, radius)
      g.setColor(playerBorderColor)
      g.drawOval(borderX, borderY, diameter, diameter)
      g.setColor(playerColor(player.id))
      val (innerX, innerY) = toScreenCenter(player.x, player.y, radius - playerInnerOffset)
      g.fillOval(innerX, innerY, diameter - playerInnerBorder, diameter - playerInnerBorder)
      g.setColor(playerBorderColor)
      val (labelX, labelY) = toScreenLabel(player.x, player.y)
      g.drawString(player.id, labelX, labelY)
