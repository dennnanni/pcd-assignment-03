package it.unibo.agar.model;

import java.io.Serializable;

public class Player extends AbstractEntity implements Serializable {
    public static final double DEFAULT_WINNING_MASS = 1000.0;

    public Player() {
    }

    public Player(final String id, final double x, final double y, final double mass) {
        super(id, x, y, mass);
    }

    public Player grow(Entity entity) {
        return new Player(getId(), getX(), getY(), getMass() + entity.getMass());
    }

    public Player moveTo(double newX, double newY) {
        return new Player(getId(), newX, newY, getMass());
    }

    public boolean isWinner() { return getMass() >= DEFAULT_WINNING_MASS; }

}
