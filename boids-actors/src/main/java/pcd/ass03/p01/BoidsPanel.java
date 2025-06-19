package pcd.ass03.p01;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoidsPanel extends JPanel {

	private final double width;
	private BoidsView view;
	private List<P2d> boidsPositions = new ArrayList<>();
    private int framerate;

    public BoidsPanel(BoidsView view, double width) {
    	this.view = view;
		this.width = width;
    }

    public void setFrameRate(int framerate) {
    	this.framerate = framerate;
    }

	public void updateBoids(List<P2d> boidsPositions) {
		this.boidsPositions = boidsPositions;
	}
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        
        var w = view.getWidth();
        var h = view.getHeight();
        var xScale = w/width;

        for (P2d boid : boidsPositions) {
        	var x = boid.x();
        	var y = boid.y();
        	int px = (int)(w/2 + x*xScale);
        	int py = (int)(h/2 - y*xScale);
            g.fillOval(px,py, 5, 5);
        }
        
        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + boidsPositions.size(), 10, 25);
        g.drawString("Framerate: " + framerate, 10, 40);
   }
}
