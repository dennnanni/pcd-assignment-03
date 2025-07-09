package it.unibo.agar.model;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Double.max;
import static java.lang.Double.min;

public class DefaultGameStateManager implements GameStateManager, Serializable {
    private static final double PLAYER_SPEED = 2.0;
    private static final int MAX_FOOD_ITEMS = 150;
    private static final Random random = new Random();
    private World world;
    private final Map<String, Position> playerDirections;
    private final Map<String, PlayerObject> playerObjects = new ConcurrentHashMap<>();


    public DefaultGameStateManager(final World initialWorld) {
        this.world = initialWorld;
        this.playerDirections = new HashMap<>();
        this.world.getPlayers().forEach(p -> playerDirections.put(p.getId(), Position.ZERO));
    }

    @Override
    public void subscribePlayer(String playerId, PlayerObject playerObject) throws RemoteException {
        world = world.addPlayer(playerId);
        System.out.println("Players: " + world.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.joining(", ")));
        playerObjects.put(playerId, playerObject);
    }

    @Override
    public void unsubscribePlayer(String playerId) throws RemoteException {
        playerObjects.remove(playerId);
        world = world.removePlayer(playerId);
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public void setPlayerDirection(final String playerId, final double dx, final double dy) {
        // Ensure player exists before setting direction
        if (world.getPlayerById(playerId).isPresent()) {
            this.playerDirections.put(playerId, Position.of(dx, dy));
        }
    }

    public void tick() {
        this.world = handleEating(moveAllPlayers(this.world));
        cleanupPlayerDirections();
        for (Map.Entry<String, PlayerObject> entry : playerObjects.entrySet()) {
            String playerId = entry.getKey();
            PlayerObject playerObject = entry.getValue();
            try {
                playerObject.updateWorld(this.world);
            } catch (RemoteException e) {
                throw new RuntimeException("Error updating world for player: " + playerId, e);
            }
        }
    }

    private World moveAllPlayers(final World currentWorld) {
        final List<Player> updatedPlayers = currentWorld.getPlayers().stream()
            .map(player -> {
                Position direction = playerDirections.getOrDefault(player.getId(), Position.ZERO);
                double newX = player.getX() + direction.x() * PLAYER_SPEED;
                double newY = player.getY() + direction.y() * PLAYER_SPEED;
                newX = min(max(newX, 0), currentWorld.getWidth());
                newY = min(max(newY, 0), currentWorld.getHeight());
                return player.moveTo(newX, newY);
            })
            .collect(Collectors.toList());

        return new World(currentWorld.getWidth(), currentWorld.getHeight(), updatedPlayers, currentWorld.getFoods());
    }

    private World handleEating(final World currentWorld) {
        List<Player> updatedPlayers = currentWorld.getPlayers().stream()
                .map(player -> growPlayer(currentWorld, player))
                .toList();

        final List<Food> foodsToRemove = currentWorld.getPlayers().stream()
                .flatMap(player -> eatenFoods(currentWorld, player).stream())
                .distinct()
                .toList();

        final List<Player> playersToRemove = currentWorld.getPlayers().stream()
                .flatMap(player -> eatenPlayers(currentWorld, player).stream())
                .distinct()
                .toList();

        playersToRemove.forEach(p -> {
			try {
				playerObjects.get(p.getId()).gameLost();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		});

        if (updatedPlayers.stream().anyMatch(Player::isWinner)) {
            String winnerId = updatedPlayers.stream().filter(Player::isWinner).findFirst().get().getId();
            updatedPlayers.forEach(p -> {
                try {
                    playerObjects.get(p.getId()).gameOver(winnerId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            playerObjects.clear();
            updatedPlayers = new ArrayList<>();
        }

        return new World(currentWorld.getWidth(), currentWorld.getHeight(), updatedPlayers, currentWorld.getFoods())
                .removeFoods(foodsToRemove)
                .removePlayers(playersToRemove);
    }

    private Player growPlayer(final World world, final Player player) {
        final Player afterFood = eatenFoods(world, player).stream()
                .reduce(player, Player::grow, (p1, p2) -> p1);

        return eatenPlayers(world, afterFood).stream()
                .reduce(afterFood, Player::grow, (p1, p2) -> p1);
    }

    private List<Food> eatenFoods(final World world, final Player player) {
        return world.getFoods().stream()
                .filter(food -> EatingManager.canEatFood(player, food))
                .toList();
    }

    private List<Player> eatenPlayers(final World world, final Player player) {
        return world.getPlayersExcludingSelf(player).stream()
                .filter(other -> EatingManager.canEatPlayer(player, other))
                .toList();
    }

    private void cleanupPlayerDirections() {
        List<String> currentPlayerIds = this.world.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        this.playerDirections.keySet().retainAll(currentPlayerIds);
        this.world.getPlayers().forEach(p ->
                playerDirections.putIfAbsent(p.getId(), Position.ZERO));
    }

}
