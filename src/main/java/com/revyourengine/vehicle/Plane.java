package com.revyourengine.vehicle;

import com.revyourengine.Mesh;
import org.joml.Vector3f;

/**
 * A Plane (aircraft) is a Vehicle that can move freely in 3D space (X, Y, Z).
 * It bounces off all six world borders including top and bottom.
 */
public class Plane extends Vehicle {

    /** Plane body half-extents for rendering and collision. */
    public static final float PLANE_HW = 0.60f;
    public static final float PLANE_HH = 0.15f;
    public static final float PLANE_HD = 0.70f;

    public Plane(String name) {
        super(name, 0.0f); // no gravity for aircraft
        this.halfWidth  = PLANE_HW;
        this.halfHeight = PLANE_HH;
        this.halfDepth  = PLANE_HD;
        // Start at mid-height
        setPosition(0, WORLD_MAX_Y / 2.0f, 0);
    }

    /** Allocates and returns a blue-ish box mesh suitable for a plane. */
    public static Mesh createMesh() {
        return Mesh.createBox(PLANE_HW, PLANE_HH, PLANE_HD, 0.15f, 0.40f, 0.85f);
    }

    // ---- direction controls -----------------------------------------------

    @Override
    public void moveLeft() {
        velocity.x = -speed;
    }

    @Override
    public void moveRight() {
        velocity.x = speed;
    }

    @Override
    public void moveUp() {
        velocity.y = speed;
    }

    @Override
    public void moveDown() {
        velocity.y = -speed;
    }

    /** Move in the +Z direction. */
    public void moveForward() {
        velocity.z = speed;
    }

    /** Move in the -Z direction. */
    public void moveBackward() {
        velocity.z = -speed;
    }

    // ---- update -----------------------------------------------------------

    @Override
    public void update(float dt) {
        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.y += velocity.y * dt;
        pos.z += velocity.z * dt;
        setPosition(pos.x, pos.y, pos.z);
        bounceOffBorders();
    }

    @Override
    protected void bounceOffBorders() {
        Vector3f pos = getPosition();
        float limitX = WORLD_SIZE  - halfWidth;
        float limitZ = WORLD_SIZE  - halfDepth;
        float minY   = halfHeight;
        float maxY   = WORLD_MAX_Y - halfHeight;

        if (pos.x >  limitX) { pos.x =  limitX; velocity.x = -Math.abs(velocity.x); }
        if (pos.x < -limitX) { pos.x = -limitX; velocity.x =  Math.abs(velocity.x); }
        if (pos.z >  limitZ) { pos.z =  limitZ; velocity.z = -Math.abs(velocity.z); }
        if (pos.z < -limitZ) { pos.z = -limitZ; velocity.z =  Math.abs(velocity.z); }
        if (pos.y >  maxY)   { pos.y =  maxY;   velocity.y = -Math.abs(velocity.y); }
        if (pos.y <  minY)   { pos.y =  minY;   velocity.y =  Math.abs(velocity.y); }

        setPosition(pos.x, pos.y, pos.z);
    }
}
