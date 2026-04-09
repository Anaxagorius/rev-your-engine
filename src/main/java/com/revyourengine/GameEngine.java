package com.revyourengine;

import com.revyourengine.gui.GuiManager;
import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.Car;
import com.revyourengine.vehicle.Plane;
import com.revyourengine.vehicle.Vehicle;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Main game loop: updates the scene, processes input, renders 3D world + GUI.
 */
public class GameEngine {

    private final Window window;
    private final Renderer renderer;
    private final Scene scene;
    private final Camera camera;
    private final GuiManager gui;
    private final CollisionLogger logger;

    private boolean running = false;

    public GameEngine() {
        logger   = new CollisionLogger();
        window   = new Window("Rev Your Engine – 3D LWJGL", 1024, 768, true);
        renderer = new Renderer();
        scene    = new Scene(logger);
        camera   = new Camera();
        gui      = new GuiManager();
    }

    public void run() {
        try {
            init();
            loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void init() throws Exception {
        window.init();

        // Camera: elevated and tilted to see the scene from above-and-front
        camera.setPosition(0, 12, 22);
        camera.setRotation(30, 0, 0);

        renderer.init();

        // Wire up GLFW key callback for Escape + T
        window.setKeyCallback((key, action) -> {
            if (action == GLFW.GLFW_PRESS) {
                if (key == GLFW.GLFW_KEY_ESCAPE) {
                    running = false;
                } else if (key == GLFW.GLFW_KEY_T) {
                    startCollisionTest();
                }
            }
        });

        gui.init(window, scene, logger,
                this::startCollisionTest,
                this::assignCarMesh,
                this::assignPlaneMesh);
    }

    private void loop() {
        running = true;
        long lastTime = System.nanoTime();

        while (running && !window.windowShouldClose()) {
            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000.0f;
            lastTime = now;
            // Guard against massive dt if app was suspended
            dt = Math.min(dt, 0.05f);

            window.pollEvents();

            scene.update(dt);

            renderer.render(window, camera, scene.getVehicles());
            gui.render(scene.getVehicles());

            window.swapBuffers();
        }
    }

    private void cleanup() {
        gui.cleanup();
        // Cleanup all vehicle meshes
        for (Vehicle v : scene.getVehicles()) {
            if (v.getMesh() != null) {
                v.getMesh().cleanup();
            }
        }
        renderer.cleanup();
        window.cleanup();
    }

    // ---- mesh assignment helpers ----------------------------------------

    private void assignCarMesh(Car car) {
        car.setMesh(Car.createMesh());
    }

    private void assignPlaneMesh(Plane plane) {
        plane.setMesh(Plane.createMesh());
    }

    // ---- T key / test ---------------------------------------------------

    private void startCollisionTest() {
        List<Vehicle> testVehicles = scene.startCollisionTest();
        for (Vehicle v : testVehicles) {
            if (v instanceof Car c) {
                c.setMesh(Car.createMesh());
            } else if (v instanceof Plane p) {
                p.setMesh(Plane.createMesh());
            }
        }
    }
}
