package com.lms.util;

import com.lms.service.LMSService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Saves and loads the whole LMS state to a file using Java serialization. */
public final class DataStore {
    private static final Path FILE = Paths.get("data", "lms.dat");

    private DataStore() { }

    public static void save(LMSService service) {
        try {
            Files.createDirectories(FILE.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(FILE))) {
                out.writeObject(service);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    public static LMSService load() {
        if (!Files.exists(FILE)) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(FILE))) {
            return (LMSService) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Warning: could not read saved data, starting fresh: " + e.getMessage());
            return null;
        }
    }
}
