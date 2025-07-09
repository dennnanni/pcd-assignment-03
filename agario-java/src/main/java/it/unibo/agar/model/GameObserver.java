package it.unibo.agar.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameObserver extends Remote {
	void updateWorld(final World world) throws RemoteException;
	void gameOver(final String winner) throws RemoteException;
}
