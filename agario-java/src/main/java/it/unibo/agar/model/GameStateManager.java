package it.unibo.agar.model;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameStateManager extends Remote, Serializable {
	void subscribeObserver(final String id, final GameObserver observer) throws RemoteException;
	void unsubscribeObserver(final String id) throws RemoteException;
	void subscribePlayer(final String playerId, final PlayerObject playerObject) throws RemoteException;
	void unsubscribePlayer(final String playerId) throws RemoteException;
	void setPlayerDirection(final String playerId, final double dx, final double dy) throws RemoteException;
	void tick() throws RemoteException;
}
