package com.revyourengine;

import com.revyourengine.vehicle.Vehicle;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

/**
 * Handles all OpenGL rendering for the 3D scene with Phong lighting.
 */
public class Renderer {

    private static final float FOV         = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR      = 0.01f;
    private static final float Z_FAR       = 1000.0f;

    // Phong lighting parameters
    private static final Vector3f LIGHT_COLOR       = new Vector3f(1.0f, 0.95f, 0.85f);
    private static final float    AMBIENT_STRENGTH  = 0.30f;
    private static final float    SPECULAR_STRENGTH = 0.45f;

    // Light position in world space (sun from upper-right-front)
    private static final Vector3f LIGHT_WORLD_POS = new Vector3f(8.0f, 18.0f, 12.0f);

    private ShaderProgram shaderProgram;
    private final Transformation transformation;
    private Mesh groundMesh;
    private Mesh borderPillarMesh;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(VERTEX_SHADER_SRC);
        shaderProgram.createFragmentShader(FRAGMENT_SHADER_SRC);
        shaderProgram.link();
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("lightPos");
        shaderProgram.createUniform("lightColor");
        shaderProgram.createUniform("ambientStrength");
        shaderProgram.createUniform("specularStrength");
        shaderProgram.createUniform("tint");

        groundMesh       = Mesh.createGround(Vehicle.WORLD_SIZE);
        borderPillarMesh = Mesh.createBox(0.12f, Vehicle.WORLD_MAX_Y / 2f, 0.12f, 0.9f, 0.85f, 0.2f);
    }

    public void render(Window window, Camera camera, List<Vehicle> vehicles,
                       List<Obstacle> obstacles) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        if (window.isResized()) {
            GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        // Projection matrix
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(
                FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        // View matrix from camera
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Transform light position into view space
        Vector3f lightViewPos = new Vector3f(LIGHT_WORLD_POS);
        lightViewPos.mulPosition(viewMatrix);
        shaderProgram.setUniform("lightPos",          lightViewPos);
        shaderProgram.setUniform("lightColor",        LIGHT_COLOR);
        shaderProgram.setUniform("ambientStrength",   AMBIENT_STRENGTH);
        shaderProgram.setUniform("specularStrength",  SPECULAR_STRENGTH);

        // Render ground
        renderMesh(groundMesh, createGroundItem(), viewMatrix, new Vector3f(1f, 1f, 1f));

        // Render corner pillars
        float s = Vehicle.WORLD_SIZE;
        float ph = Vehicle.WORLD_MAX_Y / 2f;
        float[][] corners = {{-s, ph, -s}, {s, ph, -s}, {s, ph, s}, {-s, ph, s}};
        for (float[] c : corners) {
            GameItem pillar = new GameItem(borderPillarMesh);
            pillar.setPosition(c[0], c[1], c[2]);
            renderMesh(borderPillarMesh, pillar, viewMatrix, new Vector3f(1f, 1f, 1f));
        }

        // Render obstacles
        for (Obstacle obs : obstacles) {
            if (obs.getMesh() != null) {
                renderMesh(obs.getMesh(), obs, viewMatrix, new Vector3f(1f, 1f, 1f));
            }
        }

        // Render all vehicles with per-vehicle tint (GOD MODE / flash / damage)
        for (Vehicle v : vehicles) {
            if (v.getMesh() != null) {
                Vector3f tint = computeTint(v);
                renderMesh(v.getMesh(), v, viewMatrix, tint);
            }
        }

        shaderProgram.unbind();
    }

    /**
     * Returns the colour tint to apply to a vehicle this frame.
     * - GOD mode: pulsing golden glow
     * - Collision flash: bright red-white (fades out quickly)
     * - Damaged: dimmed proportionally to remaining health
     */
    private Vector3f computeTint(Vehicle v) {
        if (v.getFlashTimer() > 0f) {
            float t = Math.min(v.getFlashTimer() / v.getFlashDuration(), 1f);
            return new Vector3f(1f + t * 0.8f, 1f - t * 0.5f, 1f - t * 0.5f);
        }
        if (v.isGodMode()) {
            double pulse = 0.9 + 0.3 * Math.sin(System.nanoTime() / 2e8);
            return new Vector3f((float) (pulse * 1.5f), (float) (pulse * 1.3f), (float) (pulse * 0.3f));
        }
        float healthRatio = v.getHealth() / v.getMaxHealth();
        float dim = 0.5f + 0.5f * healthRatio;
        return new Vector3f(dim, dim, dim);
    }

    private void renderMesh(Mesh mesh, GameItem item, Matrix4f viewMatrix, Vector3f tint) {
        Matrix4f modelViewMatrix = transformation.getModelViewMatrix(item, viewMatrix);
        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        shaderProgram.setUniform("tint", tint);
        GL30.glBindVertexArray(mesh.getVaoId());
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glEnableVertexAttribArray(2);
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private GameItem createGroundItem() {
        GameItem item = new GameItem(groundMesh);
        item.setPosition(0, 0, 0);
        return item;
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (groundMesh != null) {
            groundMesh.cleanup();
        }
        if (borderPillarMesh != null) {
            borderPillarMesh.cleanup();
        }
    }

    // ---- Embedded GLSL shaders (Phong lighting) -------------------------

    private static final String VERTEX_SHADER_SRC = """
            #version 330 core
            layout (location=0) in vec3 position;
            layout (location=1) in vec3 inColour;
            layout (location=2) in vec3 inNormal;

            out vec3 fragColour;
            out vec3 fragPos;
            out vec3 fragNormal;

            uniform mat4 projectionMatrix;
            uniform mat4 modelViewMatrix;

            void main()
            {
                vec4 mvPos    = modelViewMatrix * vec4(position, 1.0);
                gl_Position   = projectionMatrix * mvPos;
                fragColour    = inColour;
                fragPos       = mvPos.xyz;
                mat3 normalMat = mat3(transpose(inverse(modelViewMatrix)));
                fragNormal    = normalize(normalMat * inNormal);
            }
            """;

    private static final String FRAGMENT_SHADER_SRC = """
            #version 330 core
            in vec3 fragColour;
            in vec3 fragPos;
            in vec3 fragNormal;

            out vec4 outColor;

            uniform vec3  lightPos;
            uniform vec3  lightColor;
            uniform float ambientStrength;
            uniform float specularStrength;
            uniform vec3  tint;

            void main()
            {
                // Ambient
                vec3 ambient = ambientStrength * lightColor;

                // Diffuse
                vec3 norm     = normalize(fragNormal);
                vec3 lightDir = normalize(lightPos - fragPos);
                float diff    = max(dot(norm, lightDir), 0.0);
                vec3 diffuse  = diff * lightColor;

                // Specular (Blinn-Phong half-vector)
                vec3 viewDir  = normalize(-fragPos);
                vec3 halfDir  = normalize(lightDir + viewDir);
                float spec    = pow(max(dot(norm, halfDir), 0.0), 64.0);
                vec3 specular = specularStrength * spec * lightColor;

                vec3 result = (ambient + diffuse + specular) * fragColour * tint;
                outColor = vec4(clamp(result, 0.0, 1.5), 1.0);
            }
            """;
}
