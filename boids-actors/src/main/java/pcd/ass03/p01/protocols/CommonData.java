package pcd.ass03.p01.protocols;

public class CommonData {
	public record Parameters(double cohesion, double separation, double alignment) {}

	public record InitParameters(int boidsCount, Parameters parameters) {}
}
