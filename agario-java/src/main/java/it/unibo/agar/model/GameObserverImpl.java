package it.unibo.agar.model;

import it.unibo.agar.view.GlobalView;

import javax.swing.*;
import java.io.Serializable;
import java.rmi.RemoteException;

public class GameObserverImpl implements GameObserver, Serializable {
	
	private GlobalView globalView;
	
	public GameObserverImpl(GlobalView globalView) {
		this.globalView = globalView;
	}

	@Override
	public void updateWorld(World world) throws RemoteException {
		SwingUtilities.invokeLater(() -> {
			globalView.updateWorld(world);
			globalView.repaintView();
		});
	}
	
	@Override
	public void gameOver(String winner) throws RemoteException {
		SwingUtilities.invokeLater(() -> globalView.gameOver(winner));
	}
}
