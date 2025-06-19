package pcd.ass03.p01.protocols;

public class CommonData {
	public record Factors(double cohesion, double separation, double alignment) {}

	public record InitParameters(int boidsCount, Factors factors) {}
}
