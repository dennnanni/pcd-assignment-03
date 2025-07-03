package it.unibo.agar.model

sealed trait Entity:

  def id: String
  def mass: Double
  def x: Double
  def y: Double
  def radius: Double = math.sqrt(mass / math.Pi)

  def distanceTo(other: Entity): Double =
    val dx = x - other.x
    val dy = y - other.y
    math.hypot(dx, dy)

case class Player(id: String, x: Double, y: Double, mass: Double) extends Entity:

  def grow(entity: Entity): Player =
    copy(mass = mass + entity.mass)

  def grow(massToAdd: Double): Player =
    copy(mass = mass + massToAdd)

  def computeSightLimit(width: Double, height: Double, sightRadius: Double = 200.0): Seq[(Double, Double)] =
    val topLeft = (
      math.max(0, x - sightRadius),
      math.max(0, y - sightRadius)
    )
    val bottomRight = (
      math.min(width, x + sightRadius),
      math.min(height, y + sightRadius)
    )

    Seq(
      topLeft,
      (bottomRight._1, topLeft._2),
      (topLeft._1, bottomRight._2),
      bottomRight
    )

case class Food(id: String, x: Double, y: Double, mass: Double = 100.0) extends Entity

case class World(
    width: Double,
    height: Double,
    var players: Map[Coord, Seq[Player]],
    var foods: Map[Coord, Seq[Food]]
):

  private val grid = new WorldGrid(width, height, 400)

  def getGrid: WorldGrid = grid

  def playersExcludingSelf(player: Player): Seq[Player] =
    players.flatten(p => p._2).filterNot(_.id == player.id).toSeq

  def playerById(id: String): Option[Player] =
    players.flatten(p => p._2).find(_.id == id)

  def updatePlayer(player: Player): World = {
    copy(players = players.map {
      case (coord, playerList) =>
        coord -> playerList.map(p => if (p.id == player.id) player else p)
    })
  }

  def removePlayers(ids: Seq[Player]): World =
    copy(players = players.map {
      case (coord, playerList) =>
        coord -> playerList.filterNot(p => ids.map(_.id).contains(p.id))
    })

  def updateFoods(zone: Coord, newFoods: Seq[Food]): World =
    copy(foods = foods.updated(zone, newFoods))

  def updatePlayers(zone: Coord, newPlayers: Seq[Player]): World =
    copy(players = players.updated(zone, newPlayers))


object World:
  def empty: World =
    World(1000, 1000, Map.empty, Map.empty)

class WorldGrid(val width: Double, val height: Double, val cellSize: Double) {
  val cols: Int = math.ceil(width / cellSize).toInt
  val rows: Int = math.ceil(height / cellSize).toInt

  def allCoords: Seq[Coord] =
    for {
      x <- 0 until cols
      y <- 0 until rows
    } yield Coord(x, y)

  def boundsOf(coord: Coord): (Double, Double, Double, Double) =
    val minW = coord.x * cellSize
    val maxW = (minW + cellSize).min(width)
    val minH = coord.y * cellSize
    val maxH = (minH + cellSize).min(height)
    (minW, maxW, minH, maxH)

  def coordFor(x: Double, y: Double): Coord =
    Coord((x / cellSize).toInt, (y / cellSize).toInt)
}
