package com.revyourengine.vehicle;

import com.revyourengine.GameItem;
import org.joml.Vector3f;

/**
 * A moving game object with physics, health, mass, and GOD MODE.
 * Collision response uses momentum-based elastic physics.
 */
public abstract class Vehicle extends GameItem {

    /** World boundary half-extents (vehicles bounce within [-WORLD_SIZE, WORLD_SIZE] on X/Z). */
    public static final float WORLD_SIZE  = 10.0f;
    /** Maximum height for flying vehicles. */
    public static final float WORLD_MAX_Y = 8.0f;
    /** Ground level for cars (centre of car body above the ground plane). */
    public static final float GROUND_Y    = 0.25f;

    /** Maximum speed cap for normal vehicles. GOD MODE vehicles ignore this. */
    protected static final float NORMAL_MAX_SPEED = 8.0f;
    /** Maximum speed cap when GOD MODE is active. */
    protected static final float GOD_MAX_SPEED    = 20.0f;

    protected final Vector3f velocity;
    protected float speed;
    protected float acceleration;
    protected final float gravity;
    protected final String name;

    // Half-extents of the bounding box used for collision detection
    protected float halfWidth;
    protected float halfHeight;
    protected float halfDepth;

    // Physics
    protected float mass;

    // Health / damage system
    protected float health;
    protected float maxHealth;

    // GOD MODE – when active the vehicle is invincible and can reach higher speeds
    private boolean godMode = false;

    // Collision flash effect: > 0 while visible, counts down to 0
    protected float flashTimer    = 0f;
    protected float flashDuration = 0.25f;

    protected Vehicle(String name, float gravity, float mass, float maxHealth) {
        this.name      = name;
        this.velocity  = new Vector3f(0, 0, 0);
        this.speed     = 0f;
        this.acceleration = 0.5f;
        this.gravity   = gravity;
        this.mass      = mass;
        this.maxHealth = maxHealth;
        this.health    = maxHealth;
    }

    // ---- speed controls -----------------------------------------------

    public void faster() {
        float cap = godMode ? GOD_MAX_SPEED : maxSpeedCap();
        speed = Math.min(speed + acceleration, cap);
    }

    public void slower() {
        speed = Math.max(speed - acceleration, 0.0f);
    }

    /** Subclasses may override to enforce a lower per-type speed cap. */
    protected float maxSpeedCap() {
        return NORMAL_MAX_SPEED;
    }

    /** Direction controls – subclasses override as needed. */
    public abstract void moveLeft();
    public abstract void moveRight();
    public abstract void moveUp();
    public abstract void moveDown();

    // ---- update / physics ---------------------------------------------

    public void update(float dt) {
        Vector3f pos = getPosition();
        pos.x += velocity.x * dt;
        pos.y += velocity.y * dt;
        pos.z += velocity.z * dt;
        setPosition(pos.x, pos.y, pos.z);
        bounceOffBorders();

        if (flashTimer > 0f) {
            flashTimer = Math.max(0f, flashTimer - dt);
        }
    }

    protected abstract void bounceOffBorders();

    // ---- collision -------------------------------------------------------

    /**
     * AABB collision test against another vehicle.
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
     * Momentum-based elastic collision response between two vehicles.
     * Applies proportional damage based on relative impact velocity.
     * Vehicles in GOD MODE are unaffected.
     */
    public void collide(Vehicle other) {
        float totalMass = this.mass + other.mass;

        Vector3f v1 = new Vector3f(this.velocity);
        Vector3f v2 = new Vector3f(other.velocity);

        // Elastic collision: v1' = ((m1-m2)*v1 + 2*m2*v2) / (m1+m2)
        Vector3f newV1 = new Vector3f(v1)
                .mul((this.mass - other.mass) / totalMass)
                .add(new Vector3f(v2).mul(2f * other.mass / totalMass));
        Vector3f newV2 = new Vector3f(v2)
                .mul((other.mass - this.mass) / totalMass)
                .add(new Vector3f(v1).mul(2f * this.mass / totalMass));

        if (!this.godMode)  this.velocity.set(newV1);
        if (!other.godMode) other.velocity.set(newV2);

        // Damage proportional to delta-velocity and opposing mass
        float relVel = new Vector3f(v1).sub(v2).length();
        float baseDamage = relVel * 4.0f;

        if (!this.godMode)  this.takeDamage(baseDamage * (other.mass / totalMass));
        if (!other.godMode) other.takeDamage(baseDamage * (this.mass  / totalMass));

        this.flashTimer  = this.flashDuration;
        other.flashTimer = other.flashDuration;
    }

    public void takeDamage(float amount) {
        health = Math.max(0f, health - amount);
    }

    public void triggerFlash() {
        this.flashTimer = this.flashDuration;
    }

    public boolean isDestroyed() {
        return health <= 0f;
    }

    // ---- GOD MODE -------------------------------------------------------

    public void toggleGodMode() {
        godMode = !godMode;
        if (godMode) {
            health    = maxHealth;   // full heal when entering GOD MODE
            speed     = Math.min(speed * 1.5f, GOD_MAX_SPEED);
        }
    }

    // ---- getters/setters -----------------------------------------------

    public Vector3f getVelocity()                    { return velocity; }
    public void setVelocity(float x, float y, float z) { velocity.set(x, y, z); }

    public float getSpeed()                { return speed; }
    public void  setSpeed(float speed)     { this.speed = speed; }

    public float getAcceleration()         { return acceleration; }
    public void  setAcceleration(float a)  { this.acceleration = a; }

    public String getName()                { return name; }

    public float getHalfWidth()            { return halfWidth; }
    public float getHalfHeight()           { return halfHeight; }
    public float getHalfDepth()            { return halfDepth; }

    public float getMass()                 { return mass; }
    public float getHealth()               { return health; }
    public float getMaxHealth()            { return maxHealth; }
    public void  setHealth(float h)        { this.health = Math.min(h, maxHealth); }

    public boolean isGodMode()             { return godMode; }
    public float getFlashTimer()           { return flashTimer; }
    public float getFlashDuration()        { return flashDuration; }
}
