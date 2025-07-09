package it.unibo.agar.view;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GlobalView extends JFrame {

    private final GamePanel gamePanel;
    private World world;

    public GlobalView() {
        setTitle("Agar.io - Global View (Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Or DISPOSE_ON_CLOSE if multiple windows
        setPreferredSize(new Dimension(800, 800));
        this.world = World.empty();

        this.gamePanel = new GamePanel();
        add(this.gamePanel, BorderLayout.CENTER);
        gamePanel.updateWorld(world);

        pack();
        setLocationRelativeTo(null);
    }

    public void repaintView() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
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

    public void updateWorld(World world) {
        this.world = world;
        gamePanel.updateWorld(world);
    }

    public void gameOver(String winner) {
        JOptionPane optionPane = new JOptionPane(
                "Game over! The winner is: " + winner,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );

        JDialog dialog = optionPane.createDialog(null, "Game over");
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setAlwaysOnTop(false);
        dialog.setVisible(true);


        closeWindow(this);
    }

    public void open() {
        setVisible(true);
    }

    private void closeWindow(JFrame frame) {
        WindowEvent closingEvent = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
    }
}
