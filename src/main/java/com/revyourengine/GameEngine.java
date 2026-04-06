package com.revyourengine;

import com.revyourengine.gui.GuiManager;
import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.*;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Main game loop: initialises the engine, processes input, updates physics,
 * renders the 3D world + ImGui overlay, and handles cleanup.
 */
public class GameEngine {

    private final Window         window;
    private final Renderer       renderer;
    private final Scene          scene;
    private final Camera         camera;
    private final GuiManager     gui;
    private final CollisionLogger logger;

    private boolean running = false;

    public GameEngine() {
        logger   = new CollisionLogger();
        window   = new Window("Rev Your Engine – GOD MODE ⚡", 1280, 800, true);
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

        // Default FREE camera position
        camera.setPosition(0, 12, 22);
        camera.setRotation(30, 0, 0);

        renderer.init();

        // Initialise obstacle meshes now that an OpenGL context exists
        for (Obstacle obs : scene.getObstacles()) {
            obs.initMesh();
        }

        window.setKeyCallback((key, action) -> {
            if (action == GLFW.GLFW_PRESS) {
                switch (key) {
                    case GLFW.GLFW_KEY_ESCAPE -> running = false;
                    case GLFW.GLFW_KEY_T      -> startCollisionTest();
                    case GLFW.GLFW_KEY_G      -> scene.toggleGodModeSelected();
                    case GLFW.GLFW_KEY_1      -> camera.setMode(Camera.Mode.FREE);
                    case GLFW.GLFW_KEY_2      -> camera.setMode(Camera.Mode.ORBIT);
                    case GLFW.GLFW_KEY_3      -> camera.setMode(Camera.Mode.FOLLOW);
                    case GLFW.GLFW_KEY_4      -> camera.setMode(Camera.Mode.TOP_DOWN);
                    // Arrow-key control for selected vehicle
                    case GLFW.GLFW_KEY_LEFT   -> scene.moveLeftSelected();
                    case GLFW.GLFW_KEY_RIGHT  -> scene.moveRightSelected();
                    case GLFW.GLFW_KEY_UP     -> scene.moveUpSelected();
                    case GLFW.GLFW_KEY_DOWN   -> scene.moveDownSelected();
                }
            }
        });

        gui.init(window, scene, logger, camera,
                this::startCollisionTest,
                this::assignCarMesh,
                this::assignPlaneMesh,
                this::assignTruckMesh,
                this::assignHeliMesh);
    }

    private void loop() {
        running = true;
        long lastTime = System.nanoTime();

        while (running && !window.windowShouldClose()) {
            long now = System.nanoTime();
            float dt = (float) ((now - lastTime) / 1_000_000_000.0);
            lastTime = now;
            dt = Math.min(dt, 0.05f);

            window.pollEvents();

            scene.update(dt);

            // Remove destroyed vehicles and free their GPU resources
            List<Vehicle> dead = scene.removeDestroyed();
            for (Vehicle v : dead) {
                if (v.getMesh() != null) v.getMesh().cleanup();
            }

            // Update camera (handles ORBIT / FOLLOW / TOP_DOWN)
            camera.update(dt, scene.getSelectedVehicle());

            renderer.render(window, camera, scene.getVehicles(), scene.getObstacles());
            gui.render(scene.getVehicles(), dt);

            window.swapBuffers();
        }
    }

    private void cleanup() {
        gui.cleanup();
        for (Vehicle v : scene.getVehicles()) {
            if (v.getMesh() != null) v.getMesh().cleanup();
        }
        for (Obstacle obs : scene.getObstacles()) {
            if (obs.getMesh() != null) obs.getMesh().cleanup();
        }
        renderer.cleanup();
        window.cleanup();
    }

    // ---- mesh assignment helpers ----------------------------------------

    private void assignCarMesh(Car car)            { car.setMesh(Car.createMesh()); }
    private void assignPlaneMesh(Plane plane)      { plane.setMesh(Plane.createMesh()); }
    private void assignTruckMesh(Truck truck)      { truck.setMesh(Truck.createMesh()); }
    private void assignHeliMesh(Helicopter heli)   { heli.setMesh(Helicopter.createMesh()); }

    // ---- T key / test ---------------------------------------------------

    private void startCollisionTest() {
        List<Vehicle> testVehicles = scene.startCollisionTest();
        for (Vehicle v : testVehicles) {
            if      (v instanceof Car c)         assignCarMesh(c);
            else if (v instanceof Plane p)       assignPlaneMesh(p);
            else if (v instanceof Truck t)       assignTruckMesh(t);
            else if (v instanceof Helicopter h)  assignHeliMesh(h);
        }
    }
}
