package com.revyourengine;

import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.Car;
import com.revyourengine.vehicle.Plane;
import com.revyourengine.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages the collection of vehicles in the scene, runs collision detection,
 * and delegates to the collision logger.
 */
public class Scene {

    private static final Random RNG = new Random();

    private final List<Vehicle> vehicles = new ArrayList<>();
    private Vehicle lastAdded = null;
    private final CollisionLogger logger;

    // A set of pairs that are currently overlapping (to avoid repeated log spam)
    private final List<String> activeCollisions = new ArrayList<>();

    public Scene(CollisionLogger logger) {
        this.logger = logger;
    }

    // ---- factory helpers --------------------------------------------------

    /**
     * Adds a new Car at a random position with a random slow velocity.
     * Returns the new Car so the renderer can assign a mesh.
     */
    public Car addCar(String name) {
        Car car = new Car(name);
        car.setPosition(randomXZ(), Vehicle.GROUND_Y, randomXZ());
        float vx = randomSlowSpeed();
        float vz = randomSlowSpeed();
        car.setVelocity(vx, 0, vz);
        car.setSpeed(Math.max(Math.abs(vx), Math.abs(vz)));
        vehicles.add(car);
        lastAdded = car;
        logger.logEvent("Added car: " + name);
        return car;
    }

    /**
     * Adds a new Plane at a random position with a random slow 3-D velocity.
     * Returns the new Plane so the renderer can assign a mesh.
     */
    public Plane addPlane(String name) {
        Plane plane = new Plane(name);
        float y = 2.0f + RNG.nextFloat() * (Vehicle.WORLD_MAX_Y - 2.0f);
        plane.setPosition(randomXZ(), y, randomXZ());
        float vx = randomSlowSpeed();
        float vy = randomSlowSpeed() * 0.5f;
        float vz = randomSlowSpeed();
        plane.setVelocity(vx, vy, vz);
        plane.setSpeed(Math.max(Math.abs(vx), Math.max(Math.abs(vy), Math.abs(vz))));
        vehicles.add(plane);
        lastAdded = plane;
        logger.logEvent("Added plane: " + name);
        return plane;
    }

    /** Removes all vehicles from the scene. */
    public void clearAll() {
        vehicles.clear();
        activeCollisions.clear();
        lastAdded = null;
        logger.logEvent("Cleared all vehicles");
    }

    /**
     * Runs the built-in collision test:
     * two Cars heading at each other + two Planes heading at each other.
     */
    public List<Vehicle> startCollisionTest() {
        clearAll();
        List<Vehicle> created = new ArrayList<>();

        Car c1 = new Car("TestCar-A");
        c1.setPosition(-7, Vehicle.GROUND_Y, 0);
        c1.setVelocity(2.0f, 0, 0);
        c1.setSpeed(2.0f);
        vehicles.add(c1);
        created.add(c1);

        Car c2 = new Car("TestCar-B");
        c2.setPosition(7, Vehicle.GROUND_Y, 0);
        c2.setVelocity(-2.0f, 0, 0);
        c2.setSpeed(2.0f);
        vehicles.add(c2);
        created.add(c2);

        Plane p1 = new Plane("TestPlane-A");
        p1.setPosition(0, 4, -7);
        p1.setVelocity(0, 0, 2.0f);
        p1.setSpeed(2.0f);
        vehicles.add(p1);
        created.add(p1);

        Plane p2 = new Plane("TestPlane-B");
        p2.setPosition(0, 4, 7);
        p2.setVelocity(0, 0, -2.0f);
        p2.setSpeed(2.0f);
        vehicles.add(p2);
        created.add(p2);

        lastAdded = p2;
        logger.logEvent("=== Collision Test Started ===");
        return created;
    }

    // ---- update -----------------------------------------------------------

    /**
     * Updates all vehicles and checks for collisions.
     *
     * @param dt delta time in seconds
     */
    public void update(float dt) {
        for (Vehicle v : vehicles) {
            v.update(dt);
        }
        checkCollisions();
    }

    private void checkCollisions() {
        for (int i = 0; i < vehicles.size(); i++) {
            for (int j = i + 1; j < vehicles.size(); j++) {
                Vehicle a = vehicles.get(i);
                Vehicle b = vehicles.get(j);
                String pairKey = a.getName() + ":" + b.getName();

                if (a.collidesWith(b)) {
                    if (!activeCollisions.contains(pairKey)) {
                        activeCollisions.add(pairKey);
                        logger.logCollision(a.getName(), b.getName());
                        a.collide(b);
                    }
                } else {
                    activeCollisions.remove(pairKey);
                }
            }
        }
    }

    // ---- control of last-added vehicle ------------------------------------

    public void fasterLastAdded()    { if (lastAdded != null) lastAdded.faster(); }
    public void slowerLastAdded()    { if (lastAdded != null) lastAdded.slower(); }
    public void moveLeftLastAdded()  { if (lastAdded != null) lastAdded.moveLeft(); }
    public void moveRightLastAdded() { if (lastAdded != null) lastAdded.moveRight(); }
    public void moveUpLastAdded()    { if (lastAdded != null) lastAdded.moveUp(); }
    public void moveDownLastAdded()  { if (lastAdded != null) lastAdded.moveDown(); }

    // ---- getters ----------------------------------------------------------

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public Vehicle getLastAdded() {
        return lastAdded;
    }

    // ---- helpers ----------------------------------------------------------

    private float randomXZ() {
        return (RNG.nextFloat() * 2 - 1) * (Vehicle.WORLD_SIZE * 0.7f);
    }

    private float randomSlowSpeed() {
        float sign = RNG.nextBoolean() ? 1.0f : -1.0f;
        return sign * (1.0f + RNG.nextFloat() * 2.0f); // 1..3 units/sec
    }
}
