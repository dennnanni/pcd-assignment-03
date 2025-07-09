package it.unibo.agar.view;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.Player;
import it.unibo.agar.model.World;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.Optional;

public class GamePanel extends JPanel {

    private final String focusedPlayerId; // Null for global view
    private World world;

    public GamePanel(String focusedPlayerId) {
        this.focusedPlayerId = focusedPlayerId;
        this.setFocusable(true); // Important for receiving keyboard/mouse events if needed directly
    }

    public GamePanel() {
        this(null); // Constructor for GlobalView
    }

    public void updateWorld(World world) {
        this.world = world;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

		if (focusedPlayerId != null) {
            Optional<Player> playerOpt = world.getPlayerById(focusedPlayerId);
            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                final double offsetX = player.getX() - getWidth() / 2.0;
                final double offsetY = player.getY() - getHeight() / 2.0;
                AgarViewUtils.drawWorld(g2d, world, offsetX, offsetY);
            }
        } else {
            AgarViewUtils.drawWorld(g2d, world, 0, 0);
        }
    }
}
