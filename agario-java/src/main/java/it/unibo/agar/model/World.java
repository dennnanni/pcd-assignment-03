package it.unibo.agar.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class World implements Serializable {
    private final static int DEFAULT_FOOD = 150;
    private final int width;
    private final int height;
    private final List<Player> players;
    private final List<Food> foods;

    public World(int width, int height, List<Player> players, List<Food> foods) {
        this.width = width;
        this.height = height;
        this.players = List.copyOf(players); // Ensure immutability
        this.foods = List.copyOf(foods);     // Ensure immutability
    }

    public static World init(int width, int height) {
        return new World(width, height, List.of(), GameInitializer.initialFoods(DEFAULT_FOOD, width, height));
    }

    public static World empty() {
        return new World(0, 0, List.of(), List.of());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public List<Player> getPlayersExcludingSelf(final Player player) {
        return players.stream()
                .filter(p -> !p.getId().equals(player.getId()))
                .collect(Collectors.toList());
    }

    public Optional<Player> getPlayerById(final String id) {
        return players.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public World removePlayer(final String playerId) {
        List<Player> newPlayers = players.stream()
                .filter(p -> !p.getId().equals(playerId))
                .collect(Collectors.toList());
        return new World(width, height, newPlayers, foods);
    }

    public World removePlayers(final List<Player> playersToRemove) {
        List<String> idsToRemove = playersToRemove.stream().map(Player::getId).toList();
        List<Player> newPlayers = players.stream()
                .filter(p -> !idsToRemove.contains(p.getId()))
                .collect(Collectors.toList());
        return new World(width, height, newPlayers, foods);
    }

    public World removeFoods(List<Food> foodsToRemove) {
        List<Food> newFoods = foods.stream()
                .filter(f -> !foodsToRemove.contains(f)) // Assumes Food has proper equals/hashCode or relies on object identity if not overridden
                .collect(Collectors.toList());
        return new World(width, height, players, newFoods);
    }

    public World addPlayer(String playerId) {
        Player player = generateNewPlayer(playerId);
        if (players.stream().anyMatch(p -> p.getId().equals(player.getId()))) {
            throw new IllegalArgumentException("Player with ID " + player.getId() + " already exists.");
        }
        List<Player> newPlayers = new ArrayList<>(players);
        newPlayers.add(player);
        return new World(width, height, newPlayers, foods);
    }

    private Player generateNewPlayer(String playerId) {
        Random rnd = new Random();
        return new Player(playerId, rnd.nextInt(width) / 2.0, rnd.nextInt(height) / 2.0, 120.0);
    }
}
