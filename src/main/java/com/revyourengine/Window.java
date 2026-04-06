package com.revyourengine;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

/**
 * Wraps GLFW window creation, OpenGL context initialization, and input.
 */
public class Window {

    private final String title;
    private int width;
    private int height;
    private long windowHandle;
    private boolean resized;
    private boolean vSync;

    private KeyCallback keyCallback;

    public Window(String title, int width, int height, boolean vSync) {
        this.title   = title;
        this.width   = width;
        this.height  = height;
        this.vSync   = vSync;
        this.resized = false;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,   GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        windowHandle = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> {
            this.width   = w;
            this.height  = h;
            this.resized = true;
        });

        GLFW.glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (keyCallback != null) {
                keyCallback.onKey(key, action);
            }
        });

        // Center on screen
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth  = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(windowHandle, pWidth, pHeight);
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (vidMode != null) {
                GLFW.glfwSetWindowPos(windowHandle,
                        (vidMode.width()  - pWidth.get(0))  / 2,
                        (vidMode.height() - pHeight.get(0)) / 2);
            }
        }

        GLFW.glfwMakeContextCurrent(windowHandle);
        if (vSync) {
            GLFW.glfwSwapInterval(1);
        }
        GLFW.glfwShowWindow(windowHandle);

        GL.createCapabilities();
        GL11.glClearColor(0.1f, 0.2f, 0.4f, 1.0f); // sky-blue background
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public boolean windowShouldClose() {
        return GLFW.glfwWindowShouldClose(windowHandle);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(windowHandle);
    }

    public void pollEvents() {
        GLFW.glfwPollEvents();
    }

    public boolean isKeyPressed(int keyCode) {
        return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
    }

    public void cleanup() {
        Callbacks.glfwFreeCallbacks(windowHandle);
        GLFW.glfwDestroyWindow(windowHandle);
        GLFW.glfwTerminate();
        GLFWErrorCallback cb = GLFW.glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    public void setKeyCallback(KeyCallback keyCallback) {
        this.keyCallback = keyCallback;
    }

    public int getWidth()       { return width; }
    public int getHeight()      { return height; }
    public long getWindowHandle() { return windowHandle; }
    public boolean isResized()  { return resized; }
    public void setResized(boolean resized) { this.resized = resized; }

    /** Functional interface for key events. */
    @FunctionalInterface
    public interface KeyCallback {
        void onKey(int key, int action);
    }
}
