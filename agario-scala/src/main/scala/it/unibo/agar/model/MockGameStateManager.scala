package it.unibo.agar.model

trait GameStateManager:

  def getWorld: World
  def movePlayerDirection(dx: Double, dy: Double, playerId: String = "default"): Unit

class MockGameStateManager(
    var world: World,
    val speed: Double = 10.0
) extends GameStateManager:

  private var directions: Map[String, (Double, Double)] = Map.empty
  def getWorld: World = world

  // Move a player in a given direction (dx, dy)
  def movePlayerDirection(dx: Double, dy: Double, id: String = "default"): Unit =
    directions = directions.updated(id, (dx, dy))

  def tick(): Unit =
    directions.foreach:
      case (id, (dx, dy)) =>
        world.playerById(id) match
          case Some(player) =>
            world = updateWorldAfterMovement(updatePlayerPosition(player, dx, dy))
          case None =>
          // Player not found, ignore movement

  private def updatePlayerPosition(player: Player, dx: Double, dy: Double): Player =
    val newX = (player.x + dx * speed).max(0).min(world.width)
    val newY = (player.y + dy * speed).max(0).min(world.height)
    player.copy(x = newX, y = newY)

  private def updateWorldAfterMovement(player: Player): World =
    val foodEaten = world.foods.filter(food => EatingManager.canEatFood(player, food))
    val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food))
    val playersEaten = world
      .playersExcludingSelf(player)
      .filter(player => EatingManager.canEatPlayer(playerEatsFood, player))
    val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
    world
      .updatePlayer(playerEatPlayers)
      .removePlayers(playersEaten)
      .removeFoods(foodEaten)


class LocalGameStateManager(
                            var player: Player,
                            val width: Double = 1000.0,
                            val height: Double = 1000.0,
                            val speed: Double = 10.0
) extends GameStateManager:

  private var direction: (Double, Double) = (0,0)
  private var world: World = World.empty

  def getWorld: World = world
  def getPlayer: Player = player

  def updateWorld(players: Seq[Player], foods: Seq[Food]): Unit =
    world.players = world.players ++ players.filterNot { world.players.contains(_) }
    world.foods = world.foods ++ foods.filterNot { world.foods.contains(_) }

  // Move a player in a given direction (dx, dy)
  def movePlayerDirection(dx: Double, dy: Double, playerId: String = "default"): Unit =
    direction = (dx, dy)

  def tick(): Unit =
    player = updatePlayerPosition()
    world = world.updatePlayer(player)

  private def updatePlayerPosition(): Player =
    val newX = (player.x + direction._1 * speed).max(0).min(world.width)
    val newY = (player.y + direction._2 * speed).max(0).min(world.height)
    player.copy(x = newX, y = newY)

  def getPlayerSightLimit: Seq[(Double, Double)] =
    player.computeSightLimit(width, height)

  def getCoord(x: Double, y: Double): Coord =
    world.getGrid.coordFor(x, y)


object LocalGameStateManager:
  def empty: LocalGameStateManager =
    LocalGameStateManager(null) // Initial position and mass