package de.big_reddy.brigitte;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.security.auth.login.LoginException;

import de.big_reddy.brigitte.data.DatabaseManager;
import de.big_reddy.brigitte.data.models.Player;
import de.big_reddy.brigitte.data.models.Role;
import de.big_reddy.brigitte.data.models.Search;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class LFGBot implements EventListener {
    private final JDABuilder builder = new JDABuilder(AccountType.BOT);
    private final JDA jda;

    /**
     * Constructor of {@link LFGBot}. <br>
     * Initializes vital bot api and my fail, if not possible.
     *
     * @param botToken
     *            Token of application this bot shall connect to
     */
    public LFGBot(final String botToken) {
        this.builder.setToken(botToken);
        this.builder.setAutoReconnect(true);
        this.builder.setStatus(OnlineStatus.ONLINE);
        this.builder.addEventListener(this);

        // Login attempt: if it fails end program
        try {
            this.jda = this.builder.buildBlocking();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * @see net.dv8tion.jda.core.hooks.EventListener#onEvent(net.dv8tion.jda.core.
     *      events.Event)
     */
    @Override
    public void onEvent(final Event event) {
        // Private channel
        if (event instanceof PrivateMessageReceivedEvent) {
            this.onMessageReceived((PrivateMessageReceivedEvent) event);
        }
        // Server channel
        if (event instanceof MessageReceivedEvent) {}
    }

    /**
     * Handles all profile and search request interactions with user.
     *
     * @param event
     *            Event containing all data needed
     */
    public void onMessageReceived(final PrivateMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        final String message = event.getMessage().getContentRaw();
        final String id = event.getAuthor().getId();
        final List<String> answer = new ArrayList<>();

        if (message.startsWith("!search")) {
            answer.add(this.onSearch(id, message.replaceFirst("!search\\s*", "")));
        } else if (message.startsWith("!update")) {
            answer.add(this.update(id));
        } else if (message.startsWith("!delete")) {
            answer.add(this.delete(id));
        } else if (message.startsWith("!help")) {
            answer.add(this.help());
        } else if (message.startsWith("!")) {
            answer.add(this.profileUpdate(id, message));
        } else {
            answer.add("Try !help");
        }
        final String reply = answer.stream().collect(Collectors.joining("\n"));
        if (reply.isEmpty()) return;
        event.getChannel().sendMessage(reply).queue();
    }

    /**
     * Delete all occurrence of given id in the database.
     *
     * @param id
     *            ID to cleanse from the database
     * @return Message to display requester
     */
    private String delete(final String id) {
        try {
            DatabaseManager.inst().deletePlayer(id);
            DatabaseManager.inst().deleteSearches(id);
        } catch (final SQLException e) {
            e.printStackTrace();
            return "***Could not delete entry.\nPlease contact administrator.***";
        }
        return "*Player-Account deleted*";
    }

    /**
     * Reset expiration date on all date connected to given id.
     *
     * @param id
     *            ID to update
     * @return Message to display requester
     */
    private String update(final String id) {
        DatabaseManager.inst().update(id);
        return "*Expiredate reset*";
    }

    /**
     * Return message containing all {@link Player players} applicable to
     * parameters in given message.
     *
     * @param message
     *            Contains parameters for search.
     * @return All players applicable to given parameters
     */
    private String onSearch(final String id, final String message) {
        final Search search = new Search();
        search.setUserID(id);
        boolean save = false;
        boolean delete = false;
        for (final String parameter : message.split("\\s*-")) {
            if (parameter.isEmpty()) {
                continue;
            }
            final String[] temp = parameter.split("\\s+", 2);
            switch (temp[0]) {
                case "role":
                    if (temp.length < 2) return "-role *flag expects a role as second parameter, given non*";
                    final Role r = Role.getRoleByIdentifier(temp[1]);
                    if (r == null) return temp[1] + " *is no valid role*";
                    search.setRole(r);
                    break;
                case "sr":
                    try {
                        if (temp.length < 2)
                            return "-sr *flag expects a number between* 0 *and* 5000 *as second parameter, given non*";
                        final int sr = Integer.parseInt(temp[1]);
                        if (sr < 0 || sr > 5000) throw new IllegalArgumentException();
                        search.setSr(sr);
                    } catch (final IllegalArgumentException e) {
                        return "-sr *flage expects a number between* 0 *and* 5000*, given* " + temp[1];
                    }
                    break;
                case "range":
                    try {
                        if (temp.length < 2) return "-range *flag expects a number as second parameter, given non*";
                        search.setRange(Integer.parseInt(temp[1]));
                    } catch (final IllegalArgumentException e) {
                        return "-range *flage expects a number, given* " + temp[1];
                    }
                    break;
                case "delete":
                    if (save) return "-delete *and* -notify *are exclusive.\nYou may not use both*";
                    delete = true;
                    break;
                case "notify":
                    if (delete) return "-delete *and* -notify *are exclusive.\nYou may not use both*";
                    save = true;
                    break;
                default:
                    return "*Unknown flag:* " + temp[0];
            }
        }

        if (delete) {
            try {
                DatabaseManager.inst().deleteSearches(id);
                return "*Search(es) deleted.*";
            } catch (final SQLException e) {
                e.printStackTrace();
                return "***Could not delete entry(s).\nPlease contact administrator.***";
            }
        }

        final String returnV = //
                DatabaseManager //
                        .inst() //
                        .getPlayers(search) //
                        .stream() //
                        .map(p -> p.toString()) //
                        .collect(Collectors.joining("\n\n"));

        if (save) {
            try {
                DatabaseManager.inst().createSearch(search);
            } catch (final SQLException e) {
                e.printStackTrace();
                return "***Could not create entry.\nPlease contact administrator.***";
            }
        }

        return (returnV.isEmpty() ? "*No players found*" : returnV) + (save ? "\n\n*Search created*" : "");
    }

    /**
     * Update data of given {@link Player} (by id)
     *
     * @param id
     *            Player to update data
     * @param message
     *            Contains command and data to update
     * @return Summary of players profile
     */
    private String profileUpdate(final String id, final String message) {
        final DatabaseManager db = DatabaseManager.inst();
        Player player;
        try {
            player = db.getPlayerByID(id);
        } catch (final SQLException e) {
            e.printStackTrace();
            return "***Fatal error: user not found.***\n*Please contact administrator!*";
        }

        String value = message.split("\\s+", 2)[1];

        if (message.startsWith("!sr")) {
            // !sr
            try {
                final int tempValue = Integer.parseInt(value);
                if (tempValue >= 5000 || tempValue <= 0) throw new NumberFormatException();
                player.setSr(tempValue);
            } catch (final NumberFormatException e) {
                return ("*Expected a number [0-5000]:* " + value);
            }
        } else if (message.startsWith("!role")) {
            // !role
            value = value.toLowerCase();
            switch (value) {
                case "tank":
                case "dps":
                case "support":
                case "flex":
                    player.setRole(Role.getRoleByIdentifier(value));
                    break;
                default:
                    return "*Unknown role:* " + value;
            }
        } else if (message.startsWith("!description")) {
            player.setDescription(value);
        } else
            return "*Unknown command:* " + message.split("\\s+")[0];
        db.updatePlayer(player);
        this.onPlayerUpdate(player);
        return player.toString();
    }

    /**
     *
     * @param id
     * @param message
     */
    public void sendMessage(final String id, final String message) {
        try {
            final PrivateChannel channel = this.jda.getUserById(id).openPrivateChannel().complete();
            if (!channel.getMessageById(channel.getLatestMessageId()).complete().getContentRaw().equals(message)) {
                channel.sendMessage(message).queue();
            }
        } catch (final NullPointerException e) {
            e.printStackTrace();
            System.out.println("Could not send message to user: " + id);
        }
    }

    private void onPlayerUpdate(final Player player) {
        DatabaseManager.inst() //
                .getSearches(player) //
                .stream() //
                .map(Search::getUserID) //
                .forEach(id -> this.sendMessage(id, "*New players of interest are available*"));
    }

    private String help() {
        return "Help";
    }

    /**
     * Shuts down the bot.
     */
    public void shutdown() {
        this.builder.setStatus(OnlineStatus.OFFLINE);
        this.jda.shutdown();
    }
}
