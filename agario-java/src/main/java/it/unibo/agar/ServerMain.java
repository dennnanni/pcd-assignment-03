package it.unibo.agar;

import it.unibo.agar.model.DefaultGameStateManager;
import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.World;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import java.util.TimerTask;

public class ServerMain {

	private static final int WORLD_WIDTH = 1000;
	private static final int WORLD_HEIGHT = 1000;
	private static final long GAME_TICK_MS = 30; // Corresponds to ~33 FPS

	private static final String OBJ_NAME = "agar-io";

	public static void main(String[] args) {
		try {
			GameStateManager gameStateManager = new DefaultGameStateManager(World.init(WORLD_WIDTH, WORLD_HEIGHT));
			GameStateManager gameStateManagerProxy = (GameStateManager) UnicastRemoteObject.exportObject(gameStateManager, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(OBJ_NAME, gameStateManagerProxy);

			System.out.println("Object registered.");

			final Timer timer = new Timer(true); // Use daemon thread for timer
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						gameStateManager.tick();
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
				}
			}, 0, GAME_TICK_MS);

		} catch (Exception e) {
			System.err.println("Failed to start Agar server: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
