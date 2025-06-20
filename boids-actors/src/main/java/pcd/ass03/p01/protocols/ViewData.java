package pcd.ass03.p01.protocols;

public interface ViewData {
	record InitData(int boidsCount, double alignment, double separation, double cohesion) {}
	record Parameters(double alignment, double separation, double cohesion) {}
}
