package dev.avatrix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ConfigManager {
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("avatrix").resolve("configs");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static String loaded = "";

    private static class Data {
        boolean targetEsp;
        int espColor;
        int accent;
    }

    private static Path file(String name) {
        return DIR.resolve(name.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json");
    }

    public static List<String> list() {
        try {
            Files.createDirectories(DIR);
            try (var s = Files.list(DIR)) {
                return s.map(p -> p.getFileName().toString())
                        .filter(n -> n.endsWith(".json"))
                        .map(n -> n.substring(0, n.length() - 5))
                        .sorted().toList();
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public static String nextName() {
        Set<String> existing = new HashSet<>(list());
        int i = 1;
        while (existing.contains("config" + i)) i++;
        return "config" + i;
    }

    public static void save(String name) {
        Data d = new Data();
        d.targetEsp = State.targetEsp;
        d.espColor = State.espColor;
        d.accent = State.accent;
        try {
            Files.createDirectories(DIR);
            Files.writeString(file(name), GSON.toJson(d));
            loaded = name;
        } catch (IOException ignored) {}
    }

    public static void load(String name) {
        try {
            Data d = GSON.fromJson(Files.readString(file(name)), Data.class);
            if (d == null) return;
            State.targetEsp = d.targetEsp;
            State.espColor = d.espColor;
            State.accent = d.accent;
            loaded = name;
        } catch (Exception ignored) {}
    }

    public static void delete(String name) {
        try {
            Files.deleteIfExists(file(name));
            if (name.equals(loaded)) loaded = "";
        } catch (IOException ignored) {}
    }

    public static String date(String name) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(Files.getLastModifiedTime(file(name)).toMillis()));
        } catch (IOException e) {
            return "";
        }
    }
}
