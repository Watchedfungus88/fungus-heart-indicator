package dev.fungus.hearts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;

    public ConfigStore(Path path) {
        this.path = path;
    }

    public HeartConfig load() throws IOException {
        if (!Files.exists(path)) return new HeartConfig();
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            HeartConfig result = GSON.fromJson(reader, HeartConfig.class);
            if (result == null) throw new IOException("Empty settings");
            result.normalize();
            return result;
        }
    }

    public void save(HeartConfig config) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temporary = Files.createTempFile(path.toAbsolutePath().getParent(), "fungus-hearts-", ".tmp");
        try {
            Files.writeString(temporary, GSON.toJson(config.copy()) + "\n", StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
