package it.unibo.agar.prev.model

class WorldGrid(val width: Double, val height: Double, val cellSize: Double) {
  val cols: Int = (width / cellSize).toInt
  val rows: Int = (height / cellSize).toInt

  def allCoords: Seq[Coord] =
    for {
      x <- 0 until cols
      y <- 0 until rows
    } yield Coord(x, y)

  def boundsOf(coord: Coord): (Double, Double, Double, Double) =
    val minW = coord.x * cellSize
    val maxW = minW + cellSize
    val minH = coord.y * cellSize
    val maxH = minH + cellSize
    (minW, maxW, minH, maxH)

  def coordFor(x: Double, y: Double): Coord =
    Coord((x / cellSize).toInt, (y / cellSize).toInt)
}
