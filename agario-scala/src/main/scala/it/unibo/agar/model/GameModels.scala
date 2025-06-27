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

  def computeSightLimit(width: Double, height: Double): Seq[Position] =
    val sightRadius = 200.0 // TODO: make this configurable
    Seq(
      Position(x - sightRadius, y - sightRadius),
      Position(x + sightRadius, y - sightRadius),
      Position(x - sightRadius, y + sightRadius),
      Position(x + sightRadius, y + sightRadius)
    )

case class Food(id: String, x: Double, y: Double, mass: Double = 100.0) extends Entity

case class ViewWorld(
    players: Seq[Player],
    foods: Seq[Food]
):
  def playersExcludingSelf(player: Player): Seq[Player] =
    players.filterNot(_.id == player.id)

  def updatePlayer(player: Player): ViewWorld =
    val updatedPlayers = players.filterNot(_.id == player.id) :+ player
    copy(players = updatedPlayers)

  def appendPlayers(newPlayers: Seq[Player]): ViewWorld =
    copy(players = players ++ newPlayers)

  def appendFood(newFood: Seq[Food]): ViewWorld =
    copy(foods = foods ++ newFood)

object ViewWorld:
  def empty: ViewWorld =
    ViewWorld(Seq.empty, Seq.empty)
