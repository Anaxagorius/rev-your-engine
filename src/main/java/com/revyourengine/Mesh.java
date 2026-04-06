package com.revyourengine;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the OpenGL VAO/VBO data for a 3D mesh (positions, colors, indices).
 */
public class Mesh {

    private final int vaoId;
    private final List<Integer> vboIdList;
    private final int vertexCount;

    /**
     * Creates a mesh from interleaved vertex data (x, y, z, r, g, b) and indices.
     *
     * @param positions float array of vertex positions [x0,y0,z0, x1,y1,z1, ...]
     * @param colors    float array of vertex colors    [r0,g0,b0, r1,g1,b1, ...]
     * @param indices   int array of triangle indices
     */
    public Mesh(float[] positions, float[] colors, int[] indices) {
        vboIdList = new ArrayList<>();
        vertexCount = indices.length;

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Position VBO
        int posVboId = GL30.glGenBuffers();
        vboIdList.add(posVboId);
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, posVboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, posBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(posBuffer);

        // Color VBO
        int colorVboId = GL30.glGenBuffers();
        vboIdList.add(colorVboId);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, colorVboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, colorBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(colorBuffer);

        // Index VBO (EBO)
        int idxVboId = GL30.glGenBuffers();
        vboIdList.add(idxVboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, idxVboId);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL30.GL_STATIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void cleanup() {
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            GL30.glDeleteBuffers(vboId);
        }
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

    /**
     * Factory method to create a box (cuboid) mesh with a given half-extents and color.
     *
     * @param hw half-width  (x)
     * @param hh half-height (y)
     * @param hd half-depth  (z)
     * @param r  red   [0..1]
     * @param g  green [0..1]
     * @param b  blue  [0..1]
     */
    public static Mesh createBox(float hw, float hh, float hd, float r, float g, float b) {
        float[] positions = {
            // Front face
            -hw, -hh,  hd,
             hw, -hh,  hd,
             hw,  hh,  hd,
            -hw,  hh,  hd,
            // Back face
            -hw, -hh, -hd,
            -hw,  hh, -hd,
             hw,  hh, -hd,
             hw, -hh, -hd,
            // Top face
            -hw,  hh, -hd,
            -hw,  hh,  hd,
             hw,  hh,  hd,
             hw,  hh, -hd,
            // Bottom face
            -hw, -hh, -hd,
             hw, -hh, -hd,
             hw, -hh,  hd,
            -hw, -hh,  hd,
            // Right face
             hw, -hh, -hd,
             hw,  hh, -hd,
             hw,  hh,  hd,
             hw, -hh,  hd,
            // Left face
            -hw, -hh, -hd,
            -hw, -hh,  hd,
            -hw,  hh,  hd,
            -hw,  hh, -hd,
        };

        // Slightly vary colors per face for a 3D look
        float dr = r * 0.8f;
        float dg = g * 0.8f;
        float db = b * 0.8f;
        float lr = Math.min(r * 1.2f, 1.0f);
        float lg = Math.min(g * 1.2f, 1.0f);
        float lb = Math.min(b * 1.2f, 1.0f);

        float[] colors = new float[24 * 3];
        // Front face (normal color)
        for (int i = 0; i < 4; i++) { colors[i*3] = r; colors[i*3+1] = g; colors[i*3+2] = b; }
        // Back face (darker)
        for (int i = 4; i < 8; i++) { colors[i*3] = dr; colors[i*3+1] = dg; colors[i*3+2] = db; }
        // Top face (lighter)
        for (int i = 8; i < 12; i++) { colors[i*3] = lr; colors[i*3+1] = lg; colors[i*3+2] = lb; }
        // Bottom face (darker)
        for (int i = 12; i < 16; i++) { colors[i*3] = dr; colors[i*3+1] = dg; colors[i*3+2] = db; }
        // Right face (medium)
        for (int i = 16; i < 20; i++) { colors[i*3] = r * 0.9f; colors[i*3+1] = g * 0.9f; colors[i*3+2] = b * 0.9f; }
        // Left face (medium)
        for (int i = 20; i < 24; i++) { colors[i*3] = r * 0.9f; colors[i*3+1] = g * 0.9f; colors[i*3+2] = b * 0.9f; }

        int[] indices = {
            // Front
            0, 1, 2, 2, 3, 0,
            // Back
            4, 5, 6, 6, 7, 4,
            // Top
            8, 9, 10, 10, 11, 8,
            // Bottom
            12, 13, 14, 14, 15, 12,
            // Right
            16, 17, 18, 18, 19, 16,
            // Left
            20, 21, 22, 22, 23, 20,
        };

        return new Mesh(positions, colors, indices);
    }

    /**
     * Factory method to create a flat ground plane mesh.
     */
    public static Mesh createGround(float size) {
        float[] positions = {
            -size, 0, -size,
             size, 0, -size,
             size, 0,  size,
            -size, 0,  size,
        };
        float[] colors = {
            0.3f, 0.5f, 0.3f,
            0.3f, 0.5f, 0.3f,
            0.3f, 0.5f, 0.3f,
            0.3f, 0.5f, 0.3f,
        };
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        return new Mesh(positions, colors, indices);
    }
}
