package de.big_reddy.brigitte;

import java.io.File;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.big_reddy.brigitte.data.DatabaseManager;
import de.big_reddy.brigitte.data.models.Player;
import de.big_reddy.brigitte.data.models.Search;

/**
 *
 * @author Big_Reddy
 *
 */
public class Main {

    private static ScheduledExecutorService schedule;
    private static LFGBot bot;

    /**
     * Initiates essential parts of program. <br>
     * Expects Discord Bot-Token.
     *
     * @param args
     *            Has to contains a Discord Bot-token in first place
     */
    public static void main(final String args[]) {
        if (args.length < 1) throw new Error("No bot token given!");
        boolean dbReset = args.length > 1 && args[1].equals("reset");
        // Create bot
        bot = new LFGBot(args[0]);

        // Database initialization
        try {
            final File db = new File(new File("").getAbsolutePath() + "/rec/database/");
            db.mkdirs();
            final String databaseUrl = "jdbc:h2:file:" + db.getAbsolutePath() + "/brigitte.h2.db";
            final ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
            if (dbReset) {
                TableUtils.dropTable(connectionSource, Player.class, true);
                TableUtils.dropTable(connectionSource, Search.class, true);
                dbReset = false;
            }
            TableUtils.createTableIfNotExists(connectionSource, Player.class);
            TableUtils.createTableIfNotExists(connectionSource, Search.class);
            DatabaseManager.init(connectionSource);
        } catch (final SQLException e) {
            e.printStackTrace();
            throw new Error("Failed to initialize database");
        }

        // Auto-cleanup of database
        schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleAtFixedRate(Main::dbCleanUp, 0, 1, TimeUnit.DAYS);

        // CMD control
        try (final Scanner sc = new Scanner(System.in)) {
            while (true) {
                switch (sc.nextLine()) {
                    case "exit":
                        bot.shutdown();
                        break;
                    default:
                        System.out.println("Write >exit< to exit programm.");
                }
            }
        }
    }

    /**
     * Is called upon to clean the database for expired entries.
     */
    private static void dbCleanUp() {
        DatabaseManager.inst() //
                .getExpiredEntrys() //
                .stream() //
                .forEach(e -> bot.sendMessage(e.getUserID(),
                        String.format(
                                "*Your %s-Entry will be deleted in 3 days. Please use '!update' if you want to prevent this.*",
                                e.getClass().getTypeName())));
    }
}
