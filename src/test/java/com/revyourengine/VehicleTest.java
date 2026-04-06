package com.revyourengine;

import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.Car;
import com.revyourengine.vehicle.Plane;
import com.revyourengine.vehicle.Vehicle;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vehicle sub-types and Scene logic.
 * These tests do NOT start an OpenGL context – they exercise only the
 * model/physics layer.
 */
class VehicleTest {

    private static final float DT = 0.016f; // ~60 fps tick

    // ---- Car ---------------------------------------------------------------

    @Test
    void car_isSubtypeOfVehicleAndGameItem() {
        Car car = new Car("TestCar");
        assertInstanceOf(Vehicle.class, car);
        assertInstanceOf(GameItem.class, car);
    }

    @Test
    void car_startsAtGroundLevel() {
        Car car = new Car("TestCar");
        assertEquals(Vehicle.GROUND_Y, car.getPosition().y, 1e-4f);
    }

    @Test
    void car_staysOnGroundAfterUpdate() {
        Car car = new Car("TestCar");
        car.setVelocity(2, 5, 0); // give it upward velocity – should be ignored
        for (int i = 0; i < 60; i++) car.update(DT);
        assertEquals(Vehicle.GROUND_Y, car.getPosition().y, 1e-4f);
    }

    @Test
    void car_bouncesOffXBorder() {
        Car car = new Car("TestCar");
        car.setPosition(Vehicle.WORLD_SIZE - 0.1f, Vehicle.GROUND_Y, 0);
        car.setVelocity(5, 0, 0);
        for (int i = 0; i < 10; i++) car.update(DT);
        assertTrue(car.getPosition().x <= Vehicle.WORLD_SIZE);
        // After bouncing, velocity in x should become negative (reversed)
        assertTrue(car.getVelocity().x <= 0);
    }

    @Test
    void car_bouncesOffZBorder() {
        Car car = new Car("TestCar");
        car.setPosition(0, Vehicle.GROUND_Y, Vehicle.WORLD_SIZE - 0.1f);
        car.setVelocity(0, 0, 5);
        for (int i = 0; i < 10; i++) car.update(DT);
        assertTrue(car.getPosition().z <= Vehicle.WORLD_SIZE);
    }

    @Test
    void car_moveUpDoesNothing() {
        Car car = new Car("TestCar");
        car.setSpeed(3.0f);
        float yBefore = car.getPosition().y;
        car.moveUp();
        car.update(DT);
        assertEquals(yBefore, car.getPosition().y, 1e-4f);
    }

    @Test
    void car_fasterIncreasesSpeed() {
        Car car = new Car("TestCar");
        float before = car.getSpeed();
        car.faster();
        assertTrue(car.getSpeed() > before);
    }

    @Test
    void car_slowerDecreasesSpeed() {
        Car car = new Car("TestCar");
        car.setSpeed(3.0f);
        car.slower();
        assertTrue(car.getSpeed() < 3.0f);
    }

    @Test
    void car_speedDoesNotExceedMaximum() {
        Car car = new Car("TestCar");
        for (int i = 0; i < 100; i++) car.faster();
        assertTrue(car.getSpeed() <= 8.0f);
    }

    // ---- Plane -------------------------------------------------------------

    @Test
    void plane_isSubtypeOfVehicleAndGameItem() {
        Plane plane = new Plane("TestPlane");
        assertInstanceOf(Vehicle.class, plane);
        assertInstanceOf(GameItem.class, plane);
    }

    @Test
    void plane_canMoveVertically() {
        Plane plane = new Plane("TestPlane");
        plane.setPosition(0, 4, 0);
        plane.setSpeed(3.0f);
        plane.moveUp();
        float yBefore = plane.getPosition().y;
        plane.update(DT);
        assertTrue(plane.getPosition().y > yBefore);
    }

    @Test
    void plane_bouncesOffTopBorder() {
        Plane plane = new Plane("TestPlane");
        plane.setPosition(0, Vehicle.WORLD_MAX_Y - 0.1f, 0);
        plane.setVelocity(0, 5, 0);
        for (int i = 0; i < 20; i++) plane.update(DT);
        assertTrue(plane.getPosition().y <= Vehicle.WORLD_MAX_Y);
    }

    @Test
    void plane_bouncesOffBottomBorder() {
        Plane plane = new Plane("TestPlane");
        plane.setPosition(0, 0.2f, 0);
        plane.setVelocity(0, -5, 0);
        for (int i = 0; i < 20; i++) plane.update(DT);
        assertTrue(plane.getPosition().y >= 0);
    }

    // ---- Collision --------------------------------------------------------

    @Test
    void vehicles_detectCollisionWhenOverlapping() {
        Car a = new Car("A");
        Car b = new Car("B");
        a.setPosition(0, Vehicle.GROUND_Y, 0);
        b.setPosition(0.1f, Vehicle.GROUND_Y, 0); // overlapping
        assertTrue(a.collidesWith(b));
    }

    @Test
    void vehicles_noCollisionWhenFarApart() {
        Car a = new Car("A");
        Car b = new Car("B");
        a.setPosition(-5, Vehicle.GROUND_Y, 0);
        b.setPosition( 5, Vehicle.GROUND_Y, 0);
        assertFalse(a.collidesWith(b));
    }

    @Test
    void collision_swapsVelocities() {
        Car a = new Car("A");
        Car b = new Car("B");
        a.setVelocity(3, 0, 0);
        b.setVelocity(-3, 0, 0);
        a.collide(b);
        assertEquals(-3, a.getVelocity().x, 1e-4f);
        assertEquals(3, b.getVelocity().x, 1e-4f);
    }

    @Test
    void planes_detectCollision() {
        Plane p1 = new Plane("P1");
        Plane p2 = new Plane("P2");
        p1.setPosition(0, 4, 0);
        p2.setPosition(0.2f, 4, 0); // overlapping
        assertTrue(p1.collidesWith(p2));
    }

    // ---- Scene -------------------------------------------------------------

    @Test
    void scene_addCarIncreasesVehicleCount() {
        Scene scene = new Scene(new CollisionLogger());
        scene.addCar("Car1");
        assertEquals(1, scene.getVehicles().size());
    }

    @Test
    void scene_addPlaneIncreasesVehicleCount() {
        Scene scene = new Scene(new CollisionLogger());
        scene.addPlane("Plane1");
        assertEquals(1, scene.getVehicles().size());
    }

    @Test
    void scene_clearAllRemovesEverything() {
        Scene scene = new Scene(new CollisionLogger());
        scene.addCar("Car1");
        scene.addPlane("Plane1");
        scene.clearAll();
        assertEquals(0, scene.getVehicles().size());
    }

    @Test
    void scene_lastAddedTracksLastVehicle() {
        Scene scene = new Scene(new CollisionLogger());
        scene.addCar("Car1");
        Plane p = scene.addPlane("Plane1");
        assertSame(p, scene.getLastAdded());
    }

    @Test
    void scene_collisionTestCreatesFourVehicles() {
        Scene scene = new Scene(new CollisionLogger());
        List<Vehicle> testVehicles = scene.startCollisionTest();
        assertEquals(4, testVehicles.size());
        assertEquals(4, scene.getVehicles().size());
    }

    @Test
    void scene_collisionTestHasTwoCarsAndTwoPlanes() {
        Scene scene = new Scene(new CollisionLogger());
        List<Vehicle> testVehicles = scene.startCollisionTest();
        long carCount   = testVehicles.stream().filter(v -> v instanceof Car).count();
        long planeCount = testVehicles.stream().filter(v -> v instanceof Plane).count();
        assertEquals(2, carCount);
        assertEquals(2, planeCount);
    }

    @Test
    void collisionLogger_recordsCollisions() {
        CollisionLogger log = new CollisionLogger();
        log.logCollision("A", "B");
        assertEquals(1, log.getEntries().size());
        assertTrue(log.getEntries().get(0).contains("COLLISION"));
    }

    @Test
    void scene_logsCollisionWhenVehiclesOverlap() {
        CollisionLogger log = new CollisionLogger();
        Scene scene = new Scene(log);
        Car a = scene.addCar("A");
        Car b = scene.addCar("B");
        // Place them overlapping
        a.setPosition(0, Vehicle.GROUND_Y, 0);
        b.setPosition(0.1f, Vehicle.GROUND_Y, 0);
        scene.update(DT);
        // There should be at least one collision entry
        assertTrue(log.getEntries().stream().anyMatch(e -> e.contains("COLLISION")));
    }
}
