package com.revyourengine.vehicle;

import com.revyourengine.Mesh;
import org.joml.Vector3f;

/**
 * A Helicopter is an agile 3-D aircraft that hovers in place when no input is applied.
 * Lighter than a Plane, it is very maneuverable but fragile.
 * Mass: 0.6  |  Max health: 60  |  Speed cap: 8
 */
public class Helicopter extends Vehicle {

    public static final float HELI_HW = 0.55f;
    public static final float HELI_HH = 0.30f;
    public static final float HELI_HD = 0.55f;

    /** Vertical damping: if no Y input the helicopter slowly levels off. */
    private static final float HOVER_DAMP = 3.0f;

    public Helicopter(String name) {
        super(name, 0.0f, 0.6f, 60f);
        this.halfWidth  = HELI_HW;
        this.halfHeight = HELI_HH;
        this.halfDepth  = HELI_HD;
        setPosition(0, WORLD_MAX_Y * 0.4f, 0);
    }

    /** Green/teal box mesh. */
    public static Mesh createMesh() {
        return Mesh.createBox(HELI_HW, HELI_HH, HELI_HD, 0.10f, 0.70f, 0.45f);
    }

    // ---- direction controls -----------------------------------------------

    @Override public void moveLeft()  { velocity.x = -speed; }
    @Override public void moveRight() { velocity.x =  speed; }
    @Override public void moveUp()    { velocity.y =  speed; }
    @Override public void moveDown()  { velocity.y = -speed; }

    public void moveForward()  { velocity.z =  speed; }
    public void moveBackward() { velocity.z = -speed; }

    // ---- update -----------------------------------------------------------

    @Override
    public void update(float dt) {
        // Dampen vertical velocity toward hover when no vertical input is applied
        velocity.y -= velocity.y * HOVER_DAMP * dt;

        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.y += velocity.y * dt;
        pos.z += velocity.z * dt;
        setPosition(pos.x, pos.y, pos.z);
        bounceOffBorders();
        if (flashTimer > 0f) flashTimer = Math.max(0f, flashTimer - dt);
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
