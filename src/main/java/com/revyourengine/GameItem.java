package com.revyourengine;

import org.joml.Vector3f;

/**
 * Base class for all objects in the 3D scene.
 * Stores position, rotation, scale, and an optional mesh reference.
 */
public class GameItem {

    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;
    private Mesh mesh;

    public GameItem() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f(0, 0, 0);
        scale = 1.0f;
    }

    public GameItem(Mesh mesh) {
        this();
        this.mesh = mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        rotation.set(x, y, z);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }
}
