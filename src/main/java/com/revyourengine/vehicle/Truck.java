package com.revyourengine.vehicle;

import com.revyourengine.Mesh;
import org.joml.Vector3f;

/**
 * A Truck is a heavy ground vehicle: high mass, high health, lower top speed.
 * It ploughs through lighter vehicles and deals heavy collision damage.
 * Mass: 3.0  |  Max health: 200  |  Speed cap: 5
 */
public class Truck extends Vehicle {

    public static final float TRUCK_HW = 1.0f;
    public static final float TRUCK_HH = 0.45f;
    public static final float TRUCK_HD = 0.65f;

    public Truck(String name) {
        super(name, 0.0f, 3.0f, 200f);
        this.halfWidth  = TRUCK_HW;
        this.halfHeight = TRUCK_HH;
        this.halfDepth  = TRUCK_HD;
        this.acceleration = 0.3f;
        setPosition(0, GROUND_Y + TRUCK_HH - 0.25f, 0);
    }

    /** Orange box mesh. */
    public static Mesh createMesh() {
        return Mesh.createBox(TRUCK_HW, TRUCK_HH, TRUCK_HD, 0.90f, 0.50f, 0.10f);
    }

    @Override
    protected float maxSpeedCap() { return 5.0f; }

    // ---- direction controls -----------------------------------------------

    @Override public void moveLeft()  { velocity.x = -speed; velocity.z = 0; }
    @Override public void moveRight() { velocity.x =  speed; velocity.z = 0; }
    @Override public void moveUp()    { /* ground only */ }
    @Override public void moveDown()  { /* already at ground */ }

    public void moveForward()  { velocity.z =  speed; velocity.x = 0; }
    public void moveBackward() { velocity.z = -speed; velocity.x = 0; }

    // ---- update -----------------------------------------------------------

    @Override
    public void update(float dt) {
        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.z += velocity.z * dt;
        pos.y  = GROUND_Y + TRUCK_HH - 0.25f;
        setPosition(pos.x, pos.y, pos.z);
        bounceOffBorders();
        if (flashTimer > 0f) flashTimer = Math.max(0f, flashTimer - dt);
    }

    @Override
    protected void bounceOffBorders() {
        Vector3f pos = getPosition();
        float limitX = WORLD_SIZE - halfWidth;
        float limitZ = WORLD_SIZE - halfDepth;

        if (pos.x >  limitX) { pos.x =  limitX; velocity.x = -Math.abs(velocity.x); }
        if (pos.x < -limitX) { pos.x = -limitX; velocity.x =  Math.abs(velocity.x); }
        if (pos.z >  limitZ) { pos.z =  limitZ; velocity.z = -Math.abs(velocity.z); }
        if (pos.z < -limitZ) { pos.z = -limitZ; velocity.z =  Math.abs(velocity.z); }

        float groundLevel = GROUND_Y + TRUCK_HH - 0.25f;
        setPosition(pos.x, groundLevel, pos.z);
    }
}
