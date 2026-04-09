package com.revyourengine.gui;

import com.revyourengine.Scene;
import com.revyourengine.Window;
import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.Vehicle;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dear ImGui overlay providing all GUI controls:
 * - Add Car / Add Plane
 * - Clear All
 * - Faster / Slower / Left / Right / Up / Down (for last-added vehicle)
 * - Collision log display
 * - T-key test shortcut button
 */
public class GuiManager {

    private final ImGuiImplGlfw implGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3  implGl3  = new ImGuiImplGl3();

    private final AtomicInteger carCounter   = new AtomicInteger(0);
    private final AtomicInteger planeCounter = new AtomicInteger(0);

    private Scene scene;
    private CollisionLogger logger;
    private Runnable onTestRequested;
    private java.util.function.Consumer<com.revyourengine.vehicle.Car>   onCarAdded;
    private java.util.function.Consumer<com.revyourengine.vehicle.Plane> onPlaneAdded;

    public void init(Window window, Scene scene, CollisionLogger logger,
                     Runnable onTestRequested,
                     java.util.function.Consumer<com.revyourengine.vehicle.Car> onCarAdded,
                     java.util.function.Consumer<com.revyourengine.vehicle.Plane> onPlaneAdded) {
        this.scene           = scene;
        this.logger          = logger;
        this.onTestRequested = onTestRequested;
        this.onCarAdded      = onCarAdded;
        this.onPlaneAdded    = onPlaneAdded;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null); // disable imgui.ini

        implGlfw.init(window.getWindowHandle(), true);
        implGl3.init("#version 330 core");
    }

    public void render(List<Vehicle> vehicles) {
        implGlfw.newFrame();
        ImGui.newFrame();

        // ---- Main control panel ----------------------------------------
        ImGui.setNextWindowPos(10, 10, ImGuiCond.Always);
        ImGui.setNextWindowSize(240, 0, ImGuiCond.Always);
        ImGui.begin("Rev Your Engine", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);

        ImGui.text("--- Vehicles: " + vehicles.size() + " ---");
        ImGui.separator();

        if (ImGui.button("Add Car", 110, 25)) {
            addCar();
        }
        ImGui.sameLine();
        if (ImGui.button("Add Plane", 110, 25)) {
            addPlane();
        }

        if (ImGui.button("Clear All", 230, 25)) {
            scene.clearAll();
        }

        ImGui.separator();
        ImGui.text("Last Vehicle Controls:");

        if (ImGui.button("Faster", 70, 22)) {
            scene.fasterLastAdded();
        }
        ImGui.sameLine();
        if (ImGui.button("Slower", 70, 22)) {
            scene.slowerLastAdded();
        }

        ImGui.spacing();
        if (ImGui.button("Left", 70, 22)) {
            scene.moveLeftLastAdded();
        }
        ImGui.sameLine();
        if (ImGui.button("Right", 70, 22)) {
            scene.moveRightLastAdded();
        }

        ImGui.spacing();
        if (ImGui.button("Up", 70, 22)) {
            scene.moveUpLastAdded();
        }
        ImGui.sameLine();
        if (ImGui.button("Down", 70, 22)) {
            scene.moveDownLastAdded();
        }

        ImGui.separator();
        if (ImGui.button("[T] Collision Test", 230, 25)) {
            if (onTestRequested != null) onTestRequested.run();
        }
        ImGui.text("(or press T on keyboard)");

        ImGui.end();

        // ---- Collision log panel ----------------------------------------
        ImGui.setNextWindowPos(10, 350, ImGuiCond.Always);
        ImGui.setNextWindowSize(240, 200, ImGuiCond.Always);
        ImGui.begin("Collision Log",
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar);
        ImGui.text("=== Collision Log ===");
        ImGui.separator();

        List<String> entries = logger.getEntries();
        int start = Math.max(0, entries.size() - 10);
        for (int i = start; i < entries.size(); i++) {
            String entry = entries.get(i);
            if (entry.contains("COLLISION")) {
                ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, entry);
            } else {
                ImGui.text(entry);
            }
        }
        ImGui.end();

        ImGui.render();
        implGl3.renderDrawData(ImGui.getDrawData());
    }

    private void addCar() {
        String name = "Car-" + carCounter.incrementAndGet();
        var car = scene.addCar(name);
        if (onCarAdded != null) onCarAdded.accept(car);
    }

    private void addPlane() {
        String name = "Plane-" + planeCounter.incrementAndGet();
        var plane = scene.addPlane(name);
        if (onPlaneAdded != null) onPlaneAdded.accept(plane);
    }

    public void cleanup() {
        implGl3.dispose();
        implGlfw.dispose();
        ImGui.destroyContext();
    }
}
