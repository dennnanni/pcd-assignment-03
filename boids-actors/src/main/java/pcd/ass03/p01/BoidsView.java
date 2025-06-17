package pcd.ass03.p01;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import pcd.ass03.p01.protocols.ViewProtocol;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

	private static final String START = "START";
	private static final String STOP = "STOP";
	private static final String PAUSE = "PAUSE";
	private static final String RESUME = "RESUME";
	private final SimulationStateMonitor monitor;
	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private BoidsModel model;
	private int width, height;
	
	public BoidsView(BoidsModel model, SimulationStateMonitor monitor, int width, int height) {
		this.model = model;
		this.monitor = monitor;
		this.width = width;
		this.height = height;
		
		frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

		JPanel buttonsPanel = getStatePanel(model, monitor, cp);

		JPanel slidersPanel = new JPanel();
        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();

        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

		cp.add(BorderLayout.NORTH, buttonsPanel);
		cp.add(BorderLayout.SOUTH, slidersPanel);

		frame.setContentPane(cp);

        frame.setVisible(true);
	}

	public static Behavior<ViewProtocol> create() {
		return Behaviors.receive(ViewProtocol.class)
				.onMessage(ViewProtocol.Initialization.class, (pippo) -> {
					System.out.println("View initialized");
					return Behaviors.same();
				})
				.build();
	}

	private JPanel getStatePanel(BoidsModel model, SimulationStateMonitor monitor, JPanel cp) {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());

		JButton pauseButton = new JButton(PAUSE);
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(e -> {
			if (monitor.isPaused()) {
				monitor.resume();
			} else {
				monitor.pause();
			}

			updatePauseButton(pauseButton);
		});

		JTextField boidsNumberField = new JTextField(15);

		JButton startButton = new JButton(START);
		startButton.addActionListener(e -> {
			String input = boidsNumberField.getText();
			if (checkInput(input) && monitor.isStopped()) {
				model.createBoids(Integer.parseInt(input));
				pauseButton.setEnabled(true);
				monitor.start();
				boidsPanel = new BoidsPanel(this, model);
				cp.add(BorderLayout.CENTER, boidsPanel);
			} else if (!monitor.isStopped()) {
				try {
					monitor.stop();
					pauseButton.setEnabled(false);
				} catch (InterruptedException ex) {}
			}

			updateStartButton(startButton);
			updatePauseButton(pauseButton);
		});

		buttonsPanel.add(BorderLayout.WEST, boidsNumberField);
		buttonsPanel.add(BorderLayout.CENTER, startButton);
		buttonsPanel.add(BorderLayout.EAST, pauseButton);
		return buttonsPanel;
	}

	private void updateStartButton(JButton button) {
		if (monitor.isStopped()) {
			button.setText(START);
		} else {
			button.setText(STOP);
		}
	}

	private void updatePauseButton(JButton button) {
		if (monitor.isPaused()) {
			button.setText(RESUME);
		} else {
			button.setText(PAUSE);
		}
	}

	private boolean checkInput(String input) {
		try {
			int value = Integer.parseInt(input);
			return value > 0;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);        
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);
		return slider;
	}
	
	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			model.setCohesionWeight(0.1*val);
		} else {
			var val = alignmentSlider.getValue();
			model.setAlignmentWeight(0.1*val);
		}
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
