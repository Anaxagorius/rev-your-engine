package com.revyourengine.vehicle;

import com.revyourengine.Mesh;
import org.joml.Vector3f;

/**
 * A Car is a Vehicle that moves only on the ground plane (XZ).
 * It bounces off the world borders on X and Z, and cannot fly.
 * Attempting to move it upward has no effect.
 */
public class Car extends Vehicle {

    /** Car body half-extents for rendering and collision. */
    public static final float CAR_HW = 0.75f;
    public static final float CAR_HH = 0.25f;
    public static final float CAR_HD = 0.40f;

    public Car(String name) {
        super(name, 0.0f);
        this.halfWidth  = CAR_HW;
        this.halfHeight = CAR_HH;
        this.halfDepth  = CAR_HD;
        // Cars live at ground level
        setPosition(0, GROUND_Y, 0);
    }

    /** Allocates and returns a red box mesh suitable for a car. */
    public static Mesh createMesh() {
        return Mesh.createBox(CAR_HW, CAR_HH, CAR_HD, 0.85f, 0.15f, 0.15f);
    }

    // ---- direction controls -----------------------------------------------

    @Override
    public void moveLeft() {
        velocity.x = -speed;
        velocity.z = 0;
    }

    @Override
    public void moveRight() {
        velocity.x = speed;
        velocity.z = 0;
    }

    @Override
    public void moveUp() {
        // Cars cannot go up – they stay on the ground (no-op / bounce)
    }

    @Override
    public void moveDown() {
        // Already on the ground – no-op
    }

    /** Move in the +Z direction (away from the camera). */
    public void moveForward() {
        velocity.z = speed;
        velocity.x = 0;
    }

    /** Move in the -Z direction (toward the camera). */
    public void moveBackward() {
        velocity.z = -speed;
        velocity.x = 0;
    }

    // ---- update -----------------------------------------------------------

    @Override
    public void update(float dt) {
        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.z += velocity.z * dt;
        // Cars always stay at ground level
        pos.y = GROUND_Y;
        setPosition(pos.x, pos.y, pos.z);
        bounceOffBorders();
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

        setPosition(pos.x, GROUND_Y, pos.z);
    }
}
