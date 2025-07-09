package it.unibo.agar.view;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.Player;
import it.unibo.agar.model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.Optional;

public class LocalView extends JFrame {
    private static final double SENSITIVITY = 2;
    private final GamePanel gamePanel;
    private final String playerId;
    private World world;
    private final GameStateManager gameStateManager;

    public LocalView(GameStateManager gameStateManager, String playerId) {
        this.playerId = playerId;
        this.gameStateManager = gameStateManager;
        this.world = World.empty(); // Initialize world from GameStateManager

        setTitle("Agar.io - Local View (" + playerId + ") (Java)");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(600, 600));

        this.gamePanel = new GamePanel(playerId);
        add(this.gamePanel, BorderLayout.CENTER);
        this.gamePanel.updateWorld(world);

        setupMouseControls();

        pack();
        setLocationRelativeTo(null); // Center on screen
    }

    public void updateWorld(World world) {
        this.world = world;
        gamePanel.updateWorld(world);
    }

    private void setupMouseControls() {
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
				Optional<Player> playerOpt = world.getPlayerById(playerId);
				if (playerOpt.isPresent()) {
                    Point mousePos = e.getPoint();
                    // Player is always in the center of the local view
                    double viewCenterX = gamePanel.getWidth() / 2.0;
                    double viewCenterY = gamePanel.getHeight() / 2.0;

                    double dx = mousePos.x - viewCenterX;
                    double dy = mousePos.y - viewCenterY;

                    // Normalize the direction vector
                    double magnitude = Math.hypot(dx, dy);
                    try {
                        if (magnitude > 0) { // Avoid division by zero if mouse is exactly at center
                            gameStateManager.setPlayerDirection(playerId, (dx / magnitude) * SENSITIVITY, (dy / magnitude) * SENSITIVITY);
                        } else {
                            gameStateManager.setPlayerDirection(playerId, 0, 0); // Stop if mouse is at center
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    public void open() {
        setVisible(true); // Show the local view window
    }

    public void repaintView() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    public void gameOver(String winner) {
        showDialog("Game over! The winner is: " + winner, "Game Over");
        closeWindow(this);
    }

    public void gameLost() {
        showDialog("You lost the game!", "Game Lost");
        closeWindow(this);
    }

    private void showDialog(String message, String title) {
        JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );

        JDialog dialog = optionPane.createDialog(null, title);
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setAlwaysOnTop(false);
        dialog.setVisible(true);
    }

    public void setOnClose(Runnable onClose) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    onClose.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    System.exit(0); // Termina tutto
                }
            }
        });
    }

    private void closeWindow(JFrame frame) {
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
    }
}
