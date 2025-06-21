package pcd.ass03.p01.simulator;

import akka.actor.typed.ActorRef;
import pcd.ass03.p01.protocols.ModelProtocol;
import pcd.ass03.p01.protocols.ViewProtocol;

public class SimulatorState {

	private final ActorRef<ModelProtocol> model;
	private final ActorRef<ViewProtocol> view;

	public SimulatorState() {
		this.model = null;
		this.view = null;
	}

	public SimulatorState(ActorRef<ModelProtocol> model, ActorRef<ViewProtocol> view) {
		this.model = model;
		this.view = view;
	}

	public ActorRef<ModelProtocol> getModel() {
		return model;
	}

	public ActorRef<ViewProtocol> getView() {
		return view;
	}

	public SimulatorState withModel(ActorRef<ModelProtocol> model) {
		return new SimulatorState(model, this.view);
	}

	public SimulatorState withView(ActorRef<ViewProtocol> view) {
		return new SimulatorState(this.model, view);
	}
}
