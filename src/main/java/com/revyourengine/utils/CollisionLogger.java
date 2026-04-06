package com.revyourengine.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Logs collision events and other game messages to the console and an in-memory buffer
 * so the GUI can display recent events.
 */
public class CollisionLogger {

    private static final Logger LOGGER = Logger.getLogger(CollisionLogger.class.getName());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final int MAX_LOG_ENTRIES = 50;

    private final List<String> entries = new ArrayList<>();

    public void logCollision(String nameA, String nameB) {
        String timestamp = LocalDateTime.now().format(FMT);
        String message = String.format("[%s] COLLISION: %s <-> %s", timestamp, nameA, nameB);
        LOGGER.info(message);
        addEntry(message);
    }

    public void logEvent(String message) {
        String timestamp = LocalDateTime.now().format(FMT);
        String full = String.format("[%s] %s", timestamp, message);
        LOGGER.info(full);
        addEntry(full);
    }

    private void addEntry(String entry) {
        entries.add(entry);
        if (entries.size() > MAX_LOG_ENTRIES) {
            entries.remove(0);
        }
    }

    /** Returns an unmodifiable view of recent log entries (newest last). */
    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }
}
