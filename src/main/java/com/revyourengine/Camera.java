package com.revyourengine;

import com.revyourengine.vehicle.Vehicle;
import org.joml.Vector3f;

/**
 * Scene camera supporting four modes: FREE (fixed), ORBIT (auto-rotate),
 * FOLLOW (tracks selected vehicle), and TOP_DOWN (overhead).
 */
public class Camera {

    public enum Mode { FREE, ORBIT, FOLLOW, TOP_DOWN }

    private final Vector3f position;
    private final Vector3f rotation;

    private Mode   mode        = Mode.FREE;
    private double orbitAngle  = 0.0;
    private float  orbitRadius = 22.0f;
    private float  orbitHeight = 14.0f;
    private float  orbitSpeed  = 0.4f; // radians per second

    public Camera() {
        position = new Vector3f();
        rotation = new Vector3f();
    }

    /**
     * Updates the camera position/rotation each frame based on the active mode.
     *
     * @param dt           delta time in seconds
     * @param followTarget vehicle to follow (used in FOLLOW mode; may be null)
     */
    public void update(float dt, Vehicle followTarget) {
        switch (mode) {
            case ORBIT -> {
                orbitAngle += orbitSpeed * dt;
                float x = (float) (orbitRadius * Math.sin(orbitAngle));
                float z = (float) (orbitRadius * Math.cos(orbitAngle));
                position.set(x, orbitHeight, z);
                float yaw = (float) Math.toDegrees(-orbitAngle);
                rotation.set(28f, yaw, 0f);
            }
            case FOLLOW -> {
                if (followTarget != null) {
                    Vector3f tp = followTarget.getPosition();
                    position.set(tp.x, tp.y + 9f, tp.z + 15f);
                    rotation.set(26f, 0f, 0f);
                }
            }
            case TOP_DOWN -> {
                position.set(0f, 28f, 0f);
                rotation.set(90f, 0f, 0f);
            }
            default -> { /* FREE mode: position/rotation set manually */ }
        }
    }

    // ---- getters/setters -----------------------------------------------

    public Vector3f getPosition() { return position; }
    public void setPosition(float x, float y, float z) { position.set(x, y, z); }

    public Vector3f getRotation() { return rotation; }
    public void setRotation(float x, float y, float z) { rotation.set(x, y, z); }

    public Mode getMode()          { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public float getOrbitSpeed()         { return orbitSpeed; }
    public void  setOrbitSpeed(float s)  { this.orbitSpeed = s; }
}

