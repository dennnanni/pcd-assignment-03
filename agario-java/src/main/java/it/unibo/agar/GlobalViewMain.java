package it.unibo.agar;

import it.unibo.agar.model.GameObserver;
import it.unibo.agar.model.GameObserverImpl;
import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.view.GlobalView;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class GlobalViewMain {

	private final static String OBJ_NAME = "agar-io";
	private static final long GAME_TICK_MS = 30; // Corresponds to ~33 FPS

	private static GlobalView globalView;

	public static void main(String[] args) {
		String observerId = (args.length < 1) ? "observer" + UUID.randomUUID() : args[0];
		String host = (args.length < 2) ? null : args[1];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			GameStateManager c = (GameStateManager) registry.lookup(OBJ_NAME);

			globalView = new GlobalView();

			GameObserver observer = new GameObserverImpl(globalView);
			globalView.setOnClose(() -> {
				// Unexport dell'oggetto remoto
				try {
					c.unsubscribeObserver(observerId);
					UnicastRemoteObject.unexportObject(observer, true);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			});
			UnicastRemoteObject.exportObject(observer, 0);
			c.subscribeObserver(observerId, observer);

			globalView.open();

		} catch (Exception e) {
			log("Client exception: " + e.toString());
			e.printStackTrace();
		}

	}

	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Client Main ] " + msg);
	}
}
