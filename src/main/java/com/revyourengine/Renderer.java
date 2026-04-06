package com.revyourengine;

import com.revyourengine.vehicle.Vehicle;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

/**
 * Handles all OpenGL rendering for the 3D scene.
 */
public class Renderer {

    private static final float FOV         = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR      = 0.01f;
    private static final float Z_FAR       = 1000.0f;

    private ShaderProgram shaderProgram;
    private final Transformation transformation;
    private Mesh groundMesh;
    private Mesh borderMesh;

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

        groundMesh = Mesh.createGround(Vehicle.WORLD_SIZE);
        borderMesh = buildBorderMesh();
    }

    public void render(Window window, Camera camera, List<Vehicle> vehicles) {
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

        // Render ground
        renderItem(groundMesh, createGroundItem(), viewMatrix);

        // Render border indicators
        renderItem(borderMesh, createBorderItem(), viewMatrix);

        // Render all vehicles
        for (Vehicle v : vehicles) {
            if (v.getMesh() != null) {
                Matrix4f modelViewMatrix = transformation.getModelViewMatrix(v, viewMatrix);
                shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                GL30.glBindVertexArray(v.getMesh().getVaoId());
                GL30.glEnableVertexAttribArray(0);
                GL30.glEnableVertexAttribArray(1);
                GL11.glDrawElements(GL11.GL_TRIANGLES, v.getMesh().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
                GL30.glDisableVertexAttribArray(0);
                GL30.glDisableVertexAttribArray(1);
                GL30.glBindVertexArray(0);
            }
        }

        shaderProgram.unbind();
    }

    private void renderItem(Mesh mesh, GameItem item, Matrix4f viewMatrix) {
        Matrix4f modelViewMatrix = transformation.getModelViewMatrix(item, viewMatrix);
        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        GL30.glBindVertexArray(mesh.getVaoId());
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    private GameItem createGroundItem() {
        GameItem item = new GameItem(groundMesh);
        item.setPosition(0, 0, 0);
        return item;
    }

    private GameItem createBorderItem() {
        GameItem item = new GameItem(borderMesh);
        item.setPosition(0, 0, 0);
        return item;
    }

    /** Builds a wireframe-like border mesh showing the world boundaries. */
    private Mesh buildBorderMesh() {
        float s = Vehicle.WORLD_SIZE;
        float h = Vehicle.WORLD_MAX_Y;
        float c = 0.7f;
        // Four vertical corner pillars (thin boxes)
        return Mesh.createBox(0.05f, h / 2, 0.05f, c, c, 0.2f);
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (groundMesh != null) {
            groundMesh.cleanup();
        }
    }

    // ---- Embedded GLSL shaders -------------------------------------------

    private static final String VERTEX_SHADER_SRC = """
            #version 330 core
            layout (location=0) in vec3 position;
            layout (location=1) in vec3 inColour;
            out vec3 exColour;
            uniform mat4 projectionMatrix;
            uniform mat4 modelViewMatrix;
            void main()
            {
                gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                exColour = inColour;
            }
            """;

    private static final String FRAGMENT_SHADER_SRC = """
            #version 330 core
            in vec3 exColour;
            out vec4 fragColor;
            void main()
            {
                fragColor = vec4(exColour, 1.0);
            }
            """;
}
