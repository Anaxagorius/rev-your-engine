package com.revyourengine;

import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages vehicles, obstacles, collision detection, and selection.
 */
public class Scene {

    private static final Random RNG = new Random();

    private final List<Vehicle>  vehicles  = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    private Vehicle selectedVehicle = null;
    private final CollisionLogger logger;

    // Pairs currently overlapping (prevents repeated log spam)
    private final List<String> activeCollisions = new ArrayList<>();

    public Scene(CollisionLogger logger) {
        this.logger = logger;
        buildObstacles();
    }

    // ---- Obstacle setup ---------------------------------------------------

    private void buildObstacles() {
        obstacles.clear();
        Obstacle o1 = new Obstacle("Block-NW", 0.8f, 0.6f, 0.8f);
        o1.setPosition(-5f, 0.6f, -5f);
        Obstacle o2 = new Obstacle("Block-NE", 0.8f, 0.6f, 0.8f);
        o2.setPosition( 5f, 0.6f, -5f);
        Obstacle o3 = new Obstacle("Block-SE", 0.8f, 0.6f, 0.8f);
        o3.setPosition( 5f, 0.6f,  5f);
        Obstacle o4 = new Obstacle("Block-SW", 0.8f, 0.6f, 0.8f);
        o4.setPosition(-5f, 0.6f,  5f);
        Obstacle o5 = new Obstacle("Pillar-C", 0.5f, 1.5f, 0.5f);
        o5.setPosition( 0f, 1.5f,  0f);
        Collections.addAll(obstacles, o1, o2, o3, o4, o5);
    }

    // ---- factory helpers --------------------------------------------------

    public Car addCar(String name) {
        Car car = new Car(name);
        car.setPosition(randomXZ(), Vehicle.GROUND_Y, randomXZ());
        float vx = randomSlowSpeed(), vz = randomSlowSpeed();
        car.setVelocity(vx, 0, vz);
        car.setSpeed(Math.max(Math.abs(vx), Math.abs(vz)));
        vehicles.add(car);
        selectedVehicle = car;
        logger.logEvent("Added car: " + name);
        return car;
    }

    public Plane addPlane(String name) {
        Plane plane = new Plane(name);
        float y = 2.0f + RNG.nextFloat() * (Vehicle.WORLD_MAX_Y - 2.5f);
        plane.setPosition(randomXZ(), y, randomXZ());
        float vx = randomSlowSpeed(), vy = randomSlowSpeed() * 0.5f, vz = randomSlowSpeed();
        plane.setVelocity(vx, vy, vz);
        plane.setSpeed(Math.max(Math.abs(vx), Math.max(Math.abs(vy), Math.abs(vz))));
        vehicles.add(plane);
        selectedVehicle = plane;
        logger.logEvent("Added plane: " + name);
        return plane;
    }

    public Truck addTruck(String name) {
        Truck truck = new Truck(name);
        truck.setPosition(randomXZ(), Vehicle.GROUND_Y + Truck.TRUCK_HH - 0.25f, randomXZ());
        float vx = randomSlowSpeed() * 0.7f, vz = randomSlowSpeed() * 0.7f;
        truck.setVelocity(vx, 0, vz);
        truck.setSpeed(Math.max(Math.abs(vx), Math.abs(vz)));
        vehicles.add(truck);
        selectedVehicle = truck;
        logger.logEvent("Added truck: " + name);
        return truck;
    }

    public Helicopter addHelicopter(String name) {
        Helicopter heli = new Helicopter(name);
        float y = 2.0f + RNG.nextFloat() * (Vehicle.WORLD_MAX_Y - 3f);
        heli.setPosition(randomXZ(), y, randomXZ());
        float vx = randomSlowSpeed(), vz = randomSlowSpeed();
        heli.setVelocity(vx, 0, vz);
        heli.setSpeed(Math.max(Math.abs(vx), Math.abs(vz)));
        vehicles.add(heli);
        selectedVehicle = heli;
        logger.logEvent("Added helicopter: " + name);
        return heli;
    }

    /** Removes all vehicles and rebuilds obstacles. */
    public void clearAll() {
        vehicles.clear();
        activeCollisions.clear();
        selectedVehicle = null;
        buildObstacles();
        logger.logEvent("Cleared all vehicles");
    }

    /** Removes vehicles that have been destroyed (health == 0). */
    public List<Vehicle> removeDestroyed() {
        List<Vehicle> dead = new ArrayList<>();
        vehicles.removeIf(v -> {
            if (v.isDestroyed()) {
                dead.add(v);
                if (v == selectedVehicle) selectedVehicle = null;
                logger.logEvent("DESTROYED: " + v.getName());
                return true;
            }
            return false;
        });
        return dead;
    }

    /** Built-in collision test with all four vehicle types. */
    public List<Vehicle> startCollisionTest() {
        clearAll();
        List<Vehicle> created = new ArrayList<>();

        Car c1 = new Car("TestCar-A");
        c1.setPosition(-7, Vehicle.GROUND_Y, 0); c1.setVelocity(2.5f, 0, 0); c1.setSpeed(2.5f);
        Car c2 = new Car("TestCar-B");
        c2.setPosition( 7, Vehicle.GROUND_Y, 0); c2.setVelocity(-2.5f, 0, 0); c2.setSpeed(2.5f);

        Truck t1 = new Truck("TestTruck-A");
        t1.setPosition(0, Vehicle.GROUND_Y + Truck.TRUCK_HH - 0.25f, -6);
        t1.setVelocity(0, 0, 2.0f); t1.setSpeed(2.0f);

        Plane p1 = new Plane("TestPlane-A");
        p1.setPosition(0, 5, -7); p1.setVelocity(0, 0, 2.5f); p1.setSpeed(2.5f);
        Plane p2 = new Plane("TestPlane-B");
        p2.setPosition(0, 5,  7); p2.setVelocity(0, 0, -2.5f); p2.setSpeed(2.5f);

        Helicopter h1 = new Helicopter("TestHeli-A");
        h1.setPosition(-5, 3, 0); h1.setVelocity(2.0f, 0, 0); h1.setSpeed(2.0f);

        Collections.addAll(vehicles, c1, c2, t1, p1, p2, h1);
        Collections.addAll(created,  c1, c2, t1, p1, p2, h1);

        selectedVehicle = c1;
        logger.logEvent("=== GOD MODE Collision Test Started ===");
        return created;
    }

    // ---- update -----------------------------------------------------------

    public void update(float dt) {
        for (Vehicle v : vehicles) {
            v.update(dt);
        }
        checkVehicleCollisions();
        checkObstacleCollisions();
    }

    private void checkVehicleCollisions() {
        for (int i = 0; i < vehicles.size(); i++) {
            for (int j = i + 1; j < vehicles.size(); j++) {
                Vehicle a = vehicles.get(i);
                Vehicle b = vehicles.get(j);
                String key = a.getName() + ":" + b.getName();
                if (a.collidesWith(b)) {
                    if (!activeCollisions.contains(key)) {
                        activeCollisions.add(key);
                        logger.logCollision(a.getName(), b.getName());
                        a.collide(b);
                    }
                } else {
                    activeCollisions.remove(key);
                }
            }
        }
    }

    private void checkObstacleCollisions() {
        for (Vehicle v : vehicles) {
            org.joml.Vector3f pos = v.getPosition();
            for (Obstacle obs : obstacles) {
                if (obs.collidesWith(pos.x, pos.y, pos.z,
                        v.getHalfWidth(), v.getHalfHeight(), v.getHalfDepth())) {
                    // Reflect velocity away from obstacle
                    org.joml.Vector3f op = obs.getPosition();
                    float dx = pos.x - op.x, dz = pos.z - op.z;
                    if (Math.abs(dx) > Math.abs(dz)) {
                        v.getVelocity().x = Math.copySign(Math.abs(v.getVelocity().x), dx);
                    } else {
                        v.getVelocity().z = Math.copySign(Math.abs(v.getVelocity().z), dz);
                    }
                    if (!v.isGodMode()) v.takeDamage(3f);
                    v.triggerFlash();
                    logger.logEvent(v.getName() + " hit " + obs.getLabel());
                }
            }
        }
    }

    // ---- selected vehicle controls ----------------------------------------

    public void select(Vehicle v)         { selectedVehicle = v; }
    public void selectByIndex(int i)      {
        if (i >= 0 && i < vehicles.size()) selectedVehicle = vehicles.get(i);
    }

    public void fasterSelected()    { if (selectedVehicle != null) selectedVehicle.faster(); }
    public void slowerSelected()    { if (selectedVehicle != null) selectedVehicle.slower(); }
    public void moveLeftSelected()  { if (selectedVehicle != null) selectedVehicle.moveLeft(); }
    public void moveRightSelected() { if (selectedVehicle != null) selectedVehicle.moveRight(); }
    public void moveUpSelected()    { if (selectedVehicle != null) selectedVehicle.moveUp(); }
    public void moveDownSelected()  { if (selectedVehicle != null) selectedVehicle.moveDown(); }
    public void toggleGodModeSelected() {
        if (selectedVehicle != null) {
            selectedVehicle.toggleGodMode();
            logger.logEvent(selectedVehicle.getName() + " GOD MODE: " + selectedVehicle.isGodMode());
        }
    }

    // Legacy helpers kept for backward compat
    public void fasterLastAdded()    { fasterSelected(); }
    public void slowerLastAdded()    { slowerSelected(); }
    public void moveLeftLastAdded()  { moveLeftSelected(); }
    public void moveRightLastAdded() { moveRightSelected(); }
    public void moveUpLastAdded()    { moveUpSelected(); }
    public void moveDownLastAdded()  { moveDownSelected(); }

    // ---- getters ----------------------------------------------------------

    public List<Vehicle>  getVehicles()         { return Collections.unmodifiableList(vehicles); }
    public List<Obstacle> getObstacles()        { return Collections.unmodifiableList(obstacles); }
    public Vehicle        getSelectedVehicle()  { return selectedVehicle; }
    public Vehicle        getLastAdded()        { return selectedVehicle; }

    // ---- helpers ----------------------------------------------------------

    private float randomXZ() {
        return (RNG.nextFloat() * 2 - 1) * (Vehicle.WORLD_SIZE * 0.7f);
    }

    private float randomSlowSpeed() {
        float sign = RNG.nextBoolean() ? 1.0f : -1.0f;
        return sign * (1.0f + RNG.nextFloat() * 2.0f);
    }
}
