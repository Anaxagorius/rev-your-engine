# ⚡ Rev Your Engine – GOD MODE ⚡

A turbocharged 3-D physics sandbox and vehicle-collision simulator built with **Java 17**, **LWJGL 3**, and **Dear ImGui**.  
The `god-mode` branch replaces every subsystem with a supercharged upgrade.

---

## What's New in GOD MODE

| Feature | Details |
|---------|---------|
| **Phong Lighting** | Full ambient + diffuse + specular (Blinn-Phong) shader. Simulated sunlight, normal-mapped faces. |
| **Health & Damage** | Every vehicle has a health bar. Collisions deal physics-scaled damage. |
| **GOD MODE toggle** | Per-vehicle invincibility: full heal on activation, golden pulsing shimmer, unlocked speed cap (20 vs 8). |
| **Momentum Physics** | True elastic collision response (`m1v1 + m2v2 = const`). Heavy trucks barely slow down; light planes ricochet. |
| **Truck** | Heavy (mass 3×), high HP (200), low top speed (5). Ploughs through everything. |
| **Helicopter** | Agile 3-D aircraft with vertical hover damping. Lightest vehicle (mass 0.6), most fragile (HP 60). |
| **Static Obstacles** | Five sandstone blocks placed around the arena; vehicles bounce off them and take minor damage. |
| **Camera Modes** | FREE (fixed), ORBIT (auto-rotate), FOLLOW (tracks selected), TOP_DOWN (bird's eye). |
| **Destroyed Vehicles** | Vehicles at 0 HP are removed from the scene on the next tick. |
| **GOD MODE GUI** | Vehicle list with inline health bars, per-vehicle selection, GOD MODE button, camera mode selector, FPS counter. |
| **Colour Feedback** | Flash red on impact; golden pulse in GOD MODE; dim as HP drops. |

---

## Controls

| Key / Button | Action |
|---|---|
| **T** | Run the GOD MODE collision test (6 vehicles) |
| **G** | Toggle GOD MODE for the selected vehicle |
| **1** | Camera: FREE (default fixed) |
| **2** | Camera: ORBIT (auto-rotating) |
| **3** | Camera: FOLLOW (follows selected vehicle) |
| **4** | Camera: TOP\_DOWN |
| **Arrow keys** | Move selected vehicle |
| **ESC** | Quit |
| GUI buttons | Add Car / Plane / Truck / Helicopter, Clear All, Faster/Slower, directional control |

---

## Vehicle Reference

| Type | Mass | Max HP | Speed Cap | Color |
|------|------|--------|-----------|-------|
| Car | 1.0 | 100 | 8 | Red |
| Plane | 0.8 | 80 | 8 | Blue |
| Truck | 3.0 | 200 | 5 | Orange |
| Helicopter | 0.6 | 60 | 8 | Teal |

---

## Build & Run

**Requirements:** JDK 17+, Maven 3.9+

```bash
# Run all tests
mvn clean test

# Build fat JAR
mvn clean package

# Launch (Linux / macOS – needs a display)
java -jar target/rev-your-engine.jar
```

---

## Architecture

```
src/main/java/com/revyourengine/
├── Main.java               – entry point
├── GameEngine.java         – main loop, key bindings, mesh lifecycle
├── Window.java             – GLFW window + input
├── Renderer.java           – Phong shader pipeline, tint system
├── Scene.java              – vehicles, obstacles, collision detection
├── Camera.java             – FREE / ORBIT / FOLLOW / TOP_DOWN modes
├── Mesh.java               – VAO/VBO + normals (attrib 0/1/2)
├── Obstacle.java           – static world boxes
├── GameItem.java           – base scene object
├── Transformation.java     – projection / view / model matrices
├── ShaderProgram.java      – GLSL shader compiler + uniforms
├── vehicle/
│   ├── Vehicle.java        – base: mass, health, GOD MODE, momentum collision
│   ├── Car.java            – ground vehicle
│   ├── Plane.java          – 3-D aircraft
│   ├── Truck.java          – heavy ground vehicle  ← NEW
│   └── Helicopter.java     – hover aircraft        ← NEW
├── gui/
│   └── GuiManager.java     – full GOD MODE ImGui overlay
└── utils/
    └── CollisionLogger.java
```

---

## Tech Stack

| Component | Library | Version |
|-----------|---------|---------|
| Language | Java | 17 |
| Build | Maven | 3.9 |
| Graphics | LWJGL / OpenGL 3.3 | 3.3.3 |
| Math | JOML | 1.10.5 |
| GUI | Dear ImGui (imgui-java) | 1.86.11 |
| Tests | JUnit 5 | 5.10.0 |
