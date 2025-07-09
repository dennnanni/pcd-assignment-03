package it.unibo.agar;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.PlayerObject;
import it.unibo.agar.model.PlayerObjectImpl;
import it.unibo.agar.view.LocalView;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientMain {

	private final static String OBJ_NAME = "agar-io";

	private static LocalView localView;

	public static void main(String[] args) {
		String playerId = (args.length < 1) ? "player1" : args[0];
		String host = (args.length < 2) ? null : args[1];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			GameStateManager c = (GameStateManager) registry.lookup(OBJ_NAME);

			localView = new LocalView(c, playerId);

			PlayerObject playerRef = new PlayerObjectImpl(localView);
			localView.setOnClose(() -> {
				// Unexport dell'oggetto remoto
				try {
					c.unsubscribePlayer(playerId);
					UnicastRemoteObject.unexportObject(playerRef, true);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			});
			UnicastRemoteObject.exportObject(playerRef, 0);
			c.subscribePlayer(playerId, playerRef);

			localView.open();

		} catch (Exception e) {
			log("Client exception: " + e.toString());
			e.printStackTrace();
		}

	}

	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Client Main ] " + msg);
	}
}
