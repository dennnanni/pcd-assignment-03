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

  def updateWorld(zone: Coord, players: Seq[Player], foods: Seq[Food]): Unit =
    world.players = world.players.updated(zone, players)
    world.foods = world.foods.updated(zone, foods)

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
    val foodEaten = world.foods.flatten(f => f._2).filter(food => EatingManager.canEatFood(player, food))
    val playerEatsFood = foodEaten.foldLeft(player)((p, food) => null)//p.grow(food))
    val playersEaten = world
      .playersExcludingSelf(player)
      .filter(player => EatingManager.canEatPlayer(playerEatsFood, player))
    val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
    world
      .updatePlayer(playerEatPlayers)
      .removePlayers(playersEaten)
      //.removeFoods(foodEaten)


class LocalGameStateManager(
                            var player: Player,
                            val width: Double = 1000.0,
                            val height: Double = 1000.0,
                            val speed: Double = 10.0
) extends GameStateManager:

  private var direction: (Double, Double) = (0,0)
  private var world: World = World.empty
  private var worldCopy: World = World.empty

  def getWorld: World = world
  def getWorldCopy: World = worldCopy
  def getPlayer: Player = player

  def updateWorld(players: Seq[Player], zone: Coord, foods: Seq[Food]): Unit =
    world.players = world.players.updated(zone, players.filterNot(p => p.id == player.id))
    world.foods = world.foods.updated(zone, foods)

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

  def copyWorld: World =
    worldCopy = world.copy(players = world.players, foods = world.foods)
    worldCopy


object LocalGameStateManager:
  def empty: LocalGameStateManager =
    LocalGameStateManager(null) // Initial position and mass