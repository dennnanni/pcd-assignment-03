package it.unibo.agar.view;

import it.unibo.agar.model.GameStateManager;

import javax.swing.*;
import java.awt.*;

public class GlobalView extends JFrame {

    private final GamePanel gamePanel;

    public GlobalView() {
        setTitle("Agar.io - Global View (Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Or DISPOSE_ON_CLOSE if multiple windows
        setPreferredSize(new Dimension(800, 800));

        this.gamePanel = new GamePanel();
        add(this.gamePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public void repaintView() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
}
