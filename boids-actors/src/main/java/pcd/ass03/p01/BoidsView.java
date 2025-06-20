package pcd.ass03.p01;

import pcd.ass03.p01.protocols.ViewData;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.util.List;
import java.util.Hashtable;
import java.util.function.Consumer;

public class BoidsView implements ChangeListener {

	private static final String START = "START";
	private static final String STOP = "STOP";
	private static final String PAUSE = "PAUSE";
	private static final String RESUME = "RESUME";
	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private int width, height;
	private final SimulationState state;

	private Runnable stopSimulation;
	private Consumer<ViewData.InitData> startSimulation;
	private Runnable pauseSimulation;
	private Runnable resumeSimulation;
	private Consumer<ViewData.Parameters> updateParameters;

	public BoidsView(int width, int height) {
		this.width = width;
		this.height = height;
		this.state = new SimulationState();

		frame = new JFrame("Boids Simulation");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

		JPanel buttonsPanel = getStatePanel(cp);

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

	private JPanel getStatePanel(JPanel cp) {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());

		JButton pauseButton = new JButton(PAUSE);
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(e -> {
			if (state.isPaused()) {
				resumeSimulation.run();
				state.resume();
			} else {
				pauseSimulation.run();
				state.pause();
			}
			updatePauseButton(pauseButton);
		});

		JTextField boidsNumberField = new JTextField(15);

		JButton startButton = new JButton(START);
		startButton.addActionListener(e -> {
			String input = boidsNumberField.getText();
			if (checkInput(input) && state.isStopped()) {
				//model.createBoids(Integer.parseInt(input));
				startSimulation.accept(new ViewData.InitData(
						Integer.parseInt(input),
						separationSlider.getValue() * 0.1,
						alignmentSlider.getValue() * 0.1,
						cohesionSlider.getValue() * 0.1
				));
				pauseButton.setEnabled(true);
				state.start();
				boidsPanel = new BoidsPanel(this, width);
				cp.add(BorderLayout.CENTER, boidsPanel);
			} else if (!state.isStopped()) {
				state.stop();
				stopSimulation.run();
				pauseButton.setEnabled(false);
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
		if (state.isStopped()) {
			button.setText(START);
		} else {
			button.setText(STOP);
		}
	}

	private void updatePauseButton(JButton button) {
		if (state.isPaused()) {
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

	public void update(int frameRate, List<P2d> boids) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.updateBoids(boids);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		var sep = separationSlider.getValue() * 0.1;
		var co = cohesionSlider.getValue() * 0.1;
		var al = alignmentSlider.getValue() * 0.1;

		updateParameters.accept(new ViewData.Parameters(sep, al, co));
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setStartSimulation(Consumer<ViewData.InitData> startSimulation) {
		this.startSimulation = startSimulation;
	}

	public void setStopSimulation(Runnable stopSimulation) {
		this.stopSimulation = stopSimulation;
	}

	public void setPauseSimulation(Runnable pauseSimulation) {
		this.pauseSimulation = pauseSimulation;
	}

	public void setResumeSimulation(Runnable resumeSimulation) {
		this.resumeSimulation = resumeSimulation;
	}

	public void setUpdateParameters(Consumer<ViewData.Parameters> updateParameters) {
		this.updateParameters = updateParameters;
	}
}
