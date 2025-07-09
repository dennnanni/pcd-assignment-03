package it.unibo.agar.model;

import it.unibo.agar.view.LocalView;

import javax.swing.*;
import java.io.Serializable;
import java.rmi.RemoteException;

public class PlayerObjectImpl implements PlayerObject, Serializable {

	private LocalView localView;

	public PlayerObjectImpl(final LocalView localView) {
		this.localView = localView;
	}

	@Override
	public void updateWorld(World world) throws RemoteException {
		SwingUtilities.invokeLater(() -> {
			localView.updateWorld(world);
			localView.repaintView();
		});
	}

	@Override
	public void gameLost() throws RemoteException {
		SwingUtilities.invokeLater(() -> localView.gameLost());
	}

	@Override
	public void gameOver(String winner) throws RemoteException {
		SwingUtilities.invokeLater(() -> localView.gameOver(winner));
	}
}
