package com.coderwin.minesweeper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.OptionalLong;
import java.util.Properties;

/** Millisecond records; disk failures retain session records without interrupting play. */
public final class BestTimesStore {
    private final Path file;
    private final EnumMap<Difficulty, Long> times = new EnumMap<>(Difficulty.class);
    private String warning = "";

    public BestTimesStore(Path file) {
        this.file = file == null ? null : file.toAbsolutePath();
        load();
    }

    public static BestTimesStore inMemory() { return new BestTimesStore(null); }

    public static BestTimesStore userDefault() {
        try {
            String override = System.getProperty("minesweeper.dataDir");
            Path directory = override == null ? Path.of(System.getProperty("user.home"), ".minesweeper") : Path.of(override);
            return new BestTimesStore(directory.resolve("best-times.properties"));
        } catch (RuntimeException ex) {
            BestTimesStore store = inMemory();
            store.warning = "기록 저장 경로를 사용할 수 없어 이번 실행에서만 기록합니다.";
            return store;
        }
    }

    public OptionalLong best(Difficulty difficulty) {
        Long value = times.get(difficulty);
        return value == null ? OptionalLong.empty() : OptionalLong.of(value);
    }

    public String warning() { return warning; }

    public boolean record(Difficulty difficulty, long millis) {
        if (millis < 0) throw new IllegalArgumentException("Negative time");
        if (best(difficulty).isPresent() && best(difficulty).getAsLong() <= millis) return false;
        times.put(difficulty, millis);
        save();
        return true;
    }

    private void load() {
        if (file == null) return;
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(file)) {
            properties.load(stream);
            for (Difficulty difficulty : Difficulty.values()) {
                String value = properties.getProperty(difficulty.name());
                if (value == null) continue;
                try {
                    long millis = Long.parseLong(value);
                    if (millis < 0) throw new NumberFormatException();
                    times.put(difficulty, millis);
                } catch (NumberFormatException ex) {
                    warning = "일부 기록이 손상되어 해당 기록을 제외했습니다.";
                }
            }
        } catch (java.nio.file.NoSuchFileException ex) {
            // A first launch has no records yet.
        } catch (IOException | IllegalArgumentException | SecurityException ex) {
            warning = "기록을 읽을 수 없습니다. 게임은 계속할 수 있습니다.";
        }
    }

    private void save() {
        if (file == null) return;
        Path temporary = null;
        try {
            Files.createDirectories(file.getParent());
            temporary = Files.createTempFile(file.getParent(), "best-times-", ".tmp");
            Properties properties = new Properties();
            times.forEach((difficulty, millis) -> properties.setProperty(difficulty.name(), millis.toString()));
            try (OutputStream stream = Files.newOutputStream(temporary)) {
                properties.store(stream, "Minesweeper best times in milliseconds");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            warning = "";
        } catch (IOException | SecurityException ex) {
            warning = "기록 저장에 실패했습니다. 이번 실행 중 기록은 유지됩니다.";
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException | SecurityException ignored) { }
            }
        }
    }
}
