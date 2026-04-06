package com.revyourengine.gui;

import com.revyourengine.Camera;
import com.revyourengine.Scene;
import com.revyourengine.Window;
import com.revyourengine.utils.CollisionLogger;
import com.revyourengine.vehicle.*;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Full GOD MODE Dear ImGui overlay:
 * - Vehicle spawner (Car, Plane, Truck, Helicopter)
 * - Vehicle list with selection and inline health bars
 * - Selected vehicle controls + GOD MODE toggle
 * - Camera mode selector
 * - Collision log
 * - FPS counter
 */
public class GuiManager {

    private final ImGuiImplGlfw implGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3  implGl3  = new ImGuiImplGl3();

    private final AtomicInteger carCounter   = new AtomicInteger(0);
    private final AtomicInteger planeCounter = new AtomicInteger(0);
    private final AtomicInteger truckCounter = new AtomicInteger(0);
    private final AtomicInteger heliCounter  = new AtomicInteger(0);

    private Scene          scene;
    private CollisionLogger logger;
    private Camera          camera;
    private Runnable        onTestRequested;

    private Consumer<Car>         onCarAdded;
    private Consumer<Plane>       onPlaneAdded;
    private Consumer<Truck>       onTruckAdded;
    private Consumer<Helicopter>  onHeliAdded;

    // FPS tracking
    private float fpsTimer  = 0f;
    private int   fpsFrames = 0;
    private int   fpsDisplay = 0;

    public void init(Window window, Scene scene, CollisionLogger logger, Camera camera,
                     Runnable onTestRequested,
                     Consumer<Car>        onCarAdded,
                     Consumer<Plane>      onPlaneAdded,
                     Consumer<Truck>      onTruckAdded,
                     Consumer<Helicopter> onHeliAdded) {
        this.scene           = scene;
        this.logger          = logger;
        this.camera          = camera;
        this.onTestRequested = onTestRequested;
        this.onCarAdded      = onCarAdded;
        this.onPlaneAdded    = onPlaneAdded;
        this.onTruckAdded    = onTruckAdded;
        this.onHeliAdded     = onHeliAdded;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);

        implGlfw.init(window.getWindowHandle(), true);
        implGl3.init("#version 330 core");
    }

    public void render(List<Vehicle> vehicles, float dt) {
        // FPS tracking
        fpsFrames++;
        fpsTimer += dt;
        if (fpsTimer >= 1.0f) {
            fpsDisplay = fpsFrames;
            fpsFrames  = 0;
            fpsTimer  -= 1.0f;
        }

        implGlfw.newFrame();
        ImGui.newFrame();

        renderControlPanel(vehicles);
        renderLogPanel();

        ImGui.render();
        implGl3.renderDrawData(ImGui.getDrawData());
    }

    // ---- Main control panel -----------------------------------------------

    private void renderControlPanel(List<Vehicle> vehicles) {
        ImGui.setNextWindowPos(10, 10, ImGuiCond.Always);
        ImGui.setNextWindowSize(270, 0, ImGuiCond.Always);
        ImGui.begin("  ⚡ REV YOUR ENGINE – GOD MODE ⚡  ",
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove);

        // FPS + vehicle count header
        ImGui.textColored(0.4f, 1.0f, 0.4f, 1.0f, "FPS: " + fpsDisplay);
        ImGui.sameLine(100);
        ImGui.text("Vehicles: " + vehicles.size());
        ImGui.separator();

        // Spawn row 1
        if (ImGui.button("+ Car",   61, 24)) addCar();
        ImGui.sameLine();
        if (ImGui.button("+ Plane", 61, 24)) addPlane();
        ImGui.sameLine();
        if (ImGui.button("+ Truck", 61, 24)) addTruck();
        ImGui.sameLine();
        if (ImGui.button("+ Heli",  61, 24)) addHeli();

        if (ImGui.button("Clear All", 130, 24)) scene.clearAll();
        ImGui.sameLine();
        if (ImGui.button("[T] Test",  120, 24)) { if (onTestRequested != null) onTestRequested.run(); }

        ImGui.separator();
        ImGui.textColored(0.9f, 0.9f, 0.3f, 1.0f, "Camera Mode:");
        Camera.Mode cm = camera.getMode();
        pushModeColor(cm == Camera.Mode.FREE);
        if (ImGui.button("Free",    60, 22)) camera.setMode(Camera.Mode.FREE);
        ImGui.popStyleColor(3);
        ImGui.sameLine();
        pushModeColor(cm == Camera.Mode.ORBIT);
        if (ImGui.button("Orbit",   60, 22)) camera.setMode(Camera.Mode.ORBIT);
        ImGui.popStyleColor(3);
        ImGui.sameLine();
        pushModeColor(cm == Camera.Mode.FOLLOW);
        if (ImGui.button("Follow",  60, 22)) camera.setMode(Camera.Mode.FOLLOW);
        ImGui.popStyleColor(3);
        ImGui.sameLine();
        pushModeColor(cm == Camera.Mode.TOP_DOWN);
        if (ImGui.button("Top",     60, 22)) camera.setMode(Camera.Mode.TOP_DOWN);
        ImGui.popStyleColor(3);

        ImGui.separator();
        ImGui.textColored(0.9f, 0.9f, 0.3f, 1.0f, "Vehicle List (click to select):");

        Vehicle sel = scene.getSelectedVehicle();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            boolean isSelected = v == sel;

            // Highlight selected
            if (isSelected) ImGui.pushStyleColor(imgui.flag.ImGuiCol.Header, 0.25f, 0.55f, 0.80f, 0.80f);

            String label = vehicleIcon(v) + " " + v.getName()
                    + (v.isGodMode() ? " [GOD]" : "")
                    + "###v" + i;
            if (ImGui.selectable(label, isSelected)) {
                scene.selectByIndex(i);
            }

            if (isSelected) ImGui.popStyleColor();

            // Inline health bar
            float hp = v.getHealth() / v.getMaxHealth();
            float[] barColor = healthColor(hp);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.PlotHistogram, barColor[0], barColor[1], barColor[2], 1.0f);
            ImGui.progressBar(hp, 250, 5, "");
            ImGui.popStyleColor();
        }

        // Selected vehicle controls
        if (sel != null) {
            ImGui.separator();
            ImGui.textColored(0.9f, 0.9f, 0.3f, 1.0f, "Selected: " + sel.getName());

            // Health display
            float hp = sel.getHealth() / sel.getMaxHealth();
            float[] bc = healthColor(hp);
            ImGui.textColored(bc[0], bc[1], bc[2], 1.0f,
                    String.format("HP: %.0f / %.0f", sel.getHealth(), sel.getMaxHealth()));
            ImGui.sameLine();
            ImGui.text(String.format("  SPD: %.1f", sel.getSpeed()));

            // GOD MODE button
            if (sel.isGodMode()) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button,        1.0f, 0.7f, 0.0f, 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered,  1.0f, 0.85f, 0.2f, 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive,   0.9f, 0.5f, 0.0f, 1.0f);
                if (ImGui.button("⚡ GOD MODE ON  ⚡", 258, 28)) scene.toggleGodModeSelected();
                ImGui.popStyleColor(3);
            } else {
                if (ImGui.button("Enable GOD MODE", 258, 28)) scene.toggleGodModeSelected();
            }

            ImGui.spacing();
            if (ImGui.button("Faster", 80, 22)) scene.fasterSelected();
            ImGui.sameLine();
            if (ImGui.button("Slower", 80, 22)) scene.slowerSelected();

            if (ImGui.button("Left",  80, 22)) scene.moveLeftSelected();
            ImGui.sameLine();
            if (ImGui.button("Right", 80, 22)) scene.moveRightSelected();
            ImGui.sameLine();
            if (ImGui.button("Up",    55, 22)) scene.moveUpSelected();
            ImGui.sameLine();
            if (ImGui.button("Down",  55, 22)) scene.moveDownSelected();
        }

        ImGui.separator();
        ImGui.textColored(0.6f, 0.6f, 0.6f, 1.0f, "G=GodMode  1-4=Camera  T=Test  ESC=Quit");

        ImGui.end();
    }

    // ---- Collision log panel ----------------------------------------------

    private void renderLogPanel() {
        ImGui.setNextWindowPos(10, 570, ImGuiCond.Always);
        ImGui.setNextWindowSize(270, 190, ImGuiCond.Always);
        ImGui.begin("Collision Log",
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar);
        ImGui.textColored(1.0f, 0.8f, 0.2f, 1.0f, "=== Event Log ===");
        ImGui.separator();

        List<String> entries = logger.getEntries();
        int start = Math.max(0, entries.size() - 12);
        for (int i = start; i < entries.size(); i++) {
            String entry = entries.get(i);
            if (entry.contains("COLLISION")) {
                ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, entry);
            } else if (entry.contains("GOD MODE")) {
                ImGui.textColored(1.0f, 0.85f, 0.1f, 1.0f, entry);
            } else if (entry.contains("DESTROYED")) {
                ImGui.textColored(0.8f, 0.2f, 0.8f, 1.0f, entry);
            } else {
                ImGui.textColored(0.8f, 0.85f, 0.9f, 1.0f, entry);
            }
        }
        ImGui.end();
    }

    // ---- spawn helpers ----------------------------------------------------

    private void addCar() {
        var car = scene.addCar("Car-" + carCounter.incrementAndGet());
        if (onCarAdded != null) onCarAdded.accept(car);
    }

    private void addPlane() {
        var plane = scene.addPlane("Plane-" + planeCounter.incrementAndGet());
        if (onPlaneAdded != null) onPlaneAdded.accept(plane);
    }

    private void addTruck() {
        var truck = scene.addTruck("Truck-" + truckCounter.incrementAndGet());
        if (onTruckAdded != null) onTruckAdded.accept(truck);
    }

    private void addHeli() {
        var heli = scene.addHelicopter("Heli-" + heliCounter.incrementAndGet());
        if (onHeliAdded != null) onHeliAdded.accept(heli);
    }

    // ---- visual helpers ---------------------------------------------------

    private String vehicleIcon(Vehicle v) {
        if (v instanceof Truck)      return "[T]";
        if (v instanceof Helicopter) return "[H]";
        if (v instanceof Plane)      return "[P]";
        return "[C]";
    }

    /** Returns RGB for a health bar (green → yellow → red). */
    private float[] healthColor(float hp) {
        if (hp > 0.6f) return new float[]{0.1f, 0.85f, 0.1f};
        if (hp > 0.3f) return new float[]{0.9f, 0.75f, 0.05f};
        return new float[]{0.9f, 0.15f, 0.15f};
    }

    /** Pushes green or grey button colour style depending on whether mode is active. */
    private void pushModeColor(boolean active) {
        if (active) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button,       0.1f, 0.6f, 0.2f, 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 0.2f, 0.75f, 0.3f, 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive,  0.05f, 0.5f, 0.15f, 1.0f);
        } else {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button,       0.3f, 0.3f, 0.3f, 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 0.45f, 0.45f, 0.45f, 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive,  0.2f, 0.2f, 0.2f, 1.0f);
        }
    }

    public void cleanup() {
        implGl3.dispose();
        implGlfw.dispose();
        ImGui.destroyContext();
    }
}
