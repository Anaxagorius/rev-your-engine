package com.revyourengine;

import org.joml.Vector3f;

/**
 * A static (immovable) box obstacle that vehicles bounce off when they collide with it.
 * The mesh is created lazily (call {@link #initMesh()}) once an OpenGL context exists.
 */
public class Obstacle extends GameItem {

    private final float halfWidth;
    private final float halfHeight;
    private final float halfDepth;
    private final String label;

    public Obstacle(String label, float hw, float hh, float hd) {
        super();
        this.label      = label;
        this.halfWidth  = hw;
        this.halfHeight = hh;
        this.halfDepth  = hd;
        // Mesh is NOT created here – call initMesh() after an OpenGL context is ready.
    }

    /** Creates and assigns the OpenGL mesh. Must be called on the render thread. */
    public void initMesh() {
        setMesh(Mesh.createBox(halfWidth, halfHeight, halfDepth, 0.55f, 0.40f, 0.25f));
    }

    /**
     * AABB collision test: returns true if the given vehicle overlaps this obstacle.
     */
    public boolean collidesWith(float vx, float vy, float vz,
                                float vhw, float vhh, float vhd) {
        Vector3f p = getPosition();
        boolean ox = Math.abs(vx - p.x) < (vhw + halfWidth);
        boolean oy = Math.abs(vy - p.y) < (vhh + halfHeight);
        boolean oz = Math.abs(vz - p.z) < (vhd + halfDepth);
        return ox && oy && oz;
    }

    public float getHalfWidth()  { return halfWidth; }
    public float getHalfHeight() { return halfHeight; }
    public float getHalfDepth()  { return halfDepth; }
    public String getLabel()     { return label; }
}
