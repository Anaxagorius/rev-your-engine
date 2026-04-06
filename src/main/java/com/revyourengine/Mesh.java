package com.revyourengine;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the OpenGL VAO/VBO data for a 3D mesh (positions, colors, normals, indices).
 */
public class Mesh {

    private final int vaoId;
    private final List<Integer> vboIdList;
    private final int vertexCount;

    /**
     * Creates a mesh from vertex data with normals for Phong lighting.
     *
     * @param positions float array of vertex positions [x0,y0,z0, ...]
     * @param colors    float array of vertex colors    [r0,g0,b0, ...]
     * @param normals   float array of vertex normals   [nx0,ny0,nz0, ...]
     * @param indices   int array of triangle indices
     */
    public Mesh(float[] positions, float[] colors, float[] normals, int[] indices) {
        vboIdList = new ArrayList<>();
        vertexCount = indices.length;

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Position VBO (attrib 0)
        int posVboId = GL30.glGenBuffers();
        vboIdList.add(posVboId);
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, posVboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, posBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(posBuffer);

        // Color VBO (attrib 1)
        int colorVboId = GL30.glGenBuffers();
        vboIdList.add(colorVboId);
        FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, colorVboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, colorBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(colorBuffer);

        // Normal VBO (attrib 2)
        int normVboId = GL30.glGenBuffers();
        vboIdList.add(normVboId);
        FloatBuffer normBuffer = MemoryUtil.memAllocFloat(normals.length);
        normBuffer.put(normals).flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, normVboId);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, normBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(2, 3, GL30.GL_FLOAT, false, 0, 0);
        MemoryUtil.memFree(normBuffer);

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
        GL30.glDisableVertexAttribArray(2);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            GL30.glDeleteBuffers(vboId);
        }
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }

    /**
     * Factory method to create a box (cuboid) mesh with a given half-extents and color.
     * Each face has a flat normal for Phong lighting.
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
            // Front face (+Z)
            -hw, -hh,  hd,
             hw, -hh,  hd,
             hw,  hh,  hd,
            -hw,  hh,  hd,
            // Back face (-Z)
            -hw, -hh, -hd,
            -hw,  hh, -hd,
             hw,  hh, -hd,
             hw, -hh, -hd,
            // Top face (+Y)
            -hw,  hh, -hd,
            -hw,  hh,  hd,
             hw,  hh,  hd,
             hw,  hh, -hd,
            // Bottom face (-Y)
            -hw, -hh, -hd,
             hw, -hh, -hd,
             hw, -hh,  hd,
            -hw, -hh,  hd,
            // Right face (+X)
             hw, -hh, -hd,
             hw,  hh, -hd,
             hw,  hh,  hd,
             hw, -hh,  hd,
            // Left face (-X)
            -hw, -hh, -hd,
            -hw, -hh,  hd,
            -hw,  hh,  hd,
            -hw,  hh, -hd,
        };

        float[] colors = new float[24 * 3];
        for (int i = 0; i < 24; i++) {
            colors[i * 3]     = r;
            colors[i * 3 + 1] = g;
            colors[i * 3 + 2] = b;
        }

        // Per-face flat normals (4 vertices per face, same normal)
        float[] normals = new float[24 * 3];
        float[][] faceNormals = {
            { 0,  0,  1},  // Front
            { 0,  0, -1},  // Back
            { 0,  1,  0},  // Top
            { 0, -1,  0},  // Bottom
            { 1,  0,  0},  // Right
            {-1,  0,  0},  // Left
        };
        for (int f = 0; f < 6; f++) {
            for (int v = 0; v < 4; v++) {
                int idx = (f * 4 + v) * 3;
                normals[idx]     = faceNormals[f][0];
                normals[idx + 1] = faceNormals[f][1];
                normals[idx + 2] = faceNormals[f][2];
            }
        }

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

        return new Mesh(positions, colors, normals, indices);
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
            0.28f, 0.45f, 0.28f,
            0.28f, 0.45f, 0.28f,
            0.28f, 0.45f, 0.28f,
            0.28f, 0.45f, 0.28f,
        };
        float[] normals = {
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
            0, 1, 0,
        };
        int[] indices = { 0, 1, 2, 2, 3, 0 };
        return new Mesh(positions, colors, normals, indices);
    }
}
