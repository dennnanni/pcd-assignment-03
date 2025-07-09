package it.unibo.agar;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.view.GlobalView;
import it.unibo.agar.view.LocalView;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GlobalViewMain {

	private final static String OBJ_NAME = "agar-io";
	private static final long GAME_TICK_MS = 30; // Corresponds to ~33 FPS

	private static GlobalView globalView;

	public static void main(String[] args) {
		String playerId = (args.length < 1) ? "player1" : args[0];
		String host = (args.length < 2) ? null : args[1];
		try {
			Registry registry = LocateRegistry.getRegistry(host);
			GameStateManager c = (GameStateManager) registry.lookup(OBJ_NAME);

			globalView = new GlobalView();


		} catch (Exception e) {
			log("Client exception: " + e.toString());
			e.printStackTrace();
		}

	}

	private static void log(String msg) {
		System.out.println("[ " + System.currentTimeMillis() + " ][ Client Main ] " + msg);
	}
}
