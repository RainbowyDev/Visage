package net.square.utilities.logger;

import net.square.Visage;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisageLogger {

    private final Visage visage;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final File logFile;

    public VisageLogger(Visage visage) {
        this.visage = visage;
        this.logFile = this.createLogFile();
    }

    public void consoleLog(String message) {
        Bukkit.getConsoleSender().sendMessage(String.format("[Visage] %s", message));
        this.fileLog("console", String.format("[Visage] %s", message));
    }

    public void fileLog(String type, String message) {

        if (!this.visage.getConfigHandler().isBoolean("logging.file_log")) {
            return;
        }

        this.executorService.execute(() -> {
            try (FileWriter writer = new FileWriter(this.logFile, true)) {
                LocalTime time = LocalTime.now();
                int hour = time.get(ChronoField.HOUR_OF_DAY);
                int minute = time.get(ChronoField.MINUTE_OF_HOUR);
                int second = time.get(ChronoField.SECOND_OF_MINUTE);

                writer.write(String.format("[%d:%d:%d] %s: %s \n", hour, minute, second, type.toUpperCase(), message));
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private File createLogFile() {
        LocalDate date = LocalDate.now();
        int year = date.get(ChronoField.YEAR);
        int month = date.get(ChronoField.MONTH_OF_YEAR);
        int day = date.get(ChronoField.DAY_OF_MONTH);

        String formattedDate = String.format("%d-%d-%d.txt", year, month, day);
        File logFile = new File("plugins/Visage/logs/" + formattedDate);
        if (!new File("plugins/Visage/logs/").exists()) {
            //noinspection ResultOfMethodCallIgnored
            new File("plugins/Visage/logs/").mkdir();
        }
        if (!logFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logFile;
    }
}