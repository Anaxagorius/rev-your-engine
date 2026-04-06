package com.revyourengine.vehicle;

import com.revyourengine.GameItem;
import com.revyourengine.Mesh;
import org.joml.Vector3f;

/**
 * A moving game object that has position, velocity, acceleration, and gravity.
 * It can collide with other Vehicle instances via AABB intersection.
 */
public abstract class Vehicle extends GameItem {

    /** World boundary half-extents (vehicles bounce within [-WORLD_SIZE, WORLD_SIZE] on X/Z). */
    public static final float WORLD_SIZE = 10.0f;
    /** Maximum height for flying vehicles. */
    public static final float WORLD_MAX_Y = 8.0f;
    /** Ground level for cars (centre of car body above the ground plane). */
    public static final float GROUND_Y = 0.25f;

    protected final Vector3f velocity;
    protected float speed;
    protected float acceleration;
    protected final float gravity;
    protected final String name;

    // Half-extents of the bounding box used for collision detection
    protected float halfWidth;
    protected float halfHeight;
    protected float halfDepth;

    protected Vehicle(String name, float gravity) {
        this.name = name;
        this.velocity = new Vector3f(0, 0, 0);
        this.speed = 0f;
        this.acceleration = 0.5f;
        this.gravity = gravity;
    }

    // ---- speed controls -----------------------------------------------

    public void faster() {
        speed = Math.min(speed + acceleration, 8.0f);
    }

    public void slower() {
        speed = Math.max(speed - acceleration, 0.0f);
    }

    /** Direction controls – subclasses override as needed. */
    public abstract void moveLeft();
    public abstract void moveRight();
    public abstract void moveUp();
    public abstract void moveDown();

    // ---- update / physics ---------------------------------------------

    /**
     * Advances the vehicle state by {@code dt} seconds.
     *
     * @param dt delta time in seconds
     */
    public void update(float dt) {
        // Apply velocity
        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.y += velocity.y * dt;
        pos.z += velocity.z * dt;
        setPosition(pos.x, pos.y, pos.z);

        // Bounce off world borders
        bounceOffBorders();
    }

    protected abstract void bounceOffBorders();

    // ---- collision -------------------------------------------------------

    /**
     * Axis-Aligned Bounding Box collision test against another vehicle.
     *
     * @param other the other vehicle
     * @return true if the two bounding boxes overlap
     */
    public boolean collidesWith(Vehicle other) {
        Vector3f pa = this.getPosition();
        Vector3f pb = other.getPosition();

        boolean overlapX = Math.abs(pa.x - pb.x) < (this.halfWidth  + other.halfWidth);
        boolean overlapY = Math.abs(pa.y - pb.y) < (this.halfHeight + other.halfHeight);
        boolean overlapZ = Math.abs(pa.z - pb.z) < (this.halfDepth  + other.halfDepth);

        return overlapX && overlapY && overlapZ;
    }

    /**
     * Handles a collision with another vehicle (elastic-style reflection).
     * Both vehicles exchange velocity components along the separation axis.
     */
    public void collide(Vehicle other) {
        // Swap velocities for a simple elastic collision
        Vector3f tmp = new Vector3f(this.velocity);
        this.velocity.set(other.velocity);
        other.velocity.set(tmp);
    }

    // ---- getters/setters -----------------------------------------------

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(float x, float y, float z) {
        velocity.set(x, y, z);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public String getName() {
        return name;
    }

    public float getHalfWidth()  { return halfWidth; }
    public float getHalfHeight() { return halfHeight; }
    public float getHalfDepth()  { return halfDepth; }
}
