package de.big_reddy.brigitte.data;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import de.big_reddy.brigitte.data.models.Expirable;
import de.big_reddy.brigitte.data.models.Player;
import de.big_reddy.brigitte.data.models.Role;
import de.big_reddy.brigitte.data.models.Search;

/**
 * @author Big_Reddy
 *
 */
public class DatabaseManager {
    /**
     * Instance of this singleton.
     */
    private static DatabaseManager inst;
    /**
     * Database interface for interactions with {@link Player} table.
     */
    private final Dao<Player, String> playerDao;
    /**
     * Database interface for interactions with {@link Search} table.
     */
    private final Dao<Search, String> searchDao;
    /**
     * Map for named {@link QueryPair QueryPairs}. Used to easily access queries
     * and their assigned {@link SelectArg}.
     */
    private final Map<String, QueryPair> querys = new HashMap<>();

    /**
     * Constructor of this singleton.
     *
     * @param connectionSource
     *            Database connection
     * @throws SQLException
     */
    private DatabaseManager(final ConnectionSource connectionSource) throws SQLException {
        this.playerDao = DaoManager.createDao(connectionSource, Player.class);
        this.searchDao = DaoManager.createDao(connectionSource, Search.class);
        this.initialForms();
    }

    /**
     * Initializes, prepares and registers all form further forms used for
     * database queries.
     *
     * @throws SQLException
     */
    private void initialForms() throws SQLException {
        // Select all player
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            this.querys.put("allPlayers", new QueryPair<>(queryBuilder.prepare()));
            queryBuilder.reset();
        }
        // Select players by role
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            final Where<Player, String> where = queryBuilder.where();
            final SelectArg arg = new SelectArg();
            where.eq("role", arg);
            this.querys.put("playersByRole", new QueryPair<>(queryBuilder.prepare(), arg));
            queryBuilder.reset();
        }
        // Select players by sr
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            final Where<Player, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            final SelectArg arg2 = new SelectArg();
            where.between("sr", arg1, arg2);
            this.querys.put("playersBySR", new QueryPair<>(queryBuilder.prepare(), arg1, arg2));
            queryBuilder.reset();
        }
        // Select players by role and sr
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            final Where<Player, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            final SelectArg arg2 = new SelectArg();
            final SelectArg arg3 = new SelectArg();
            where.eq("role", arg1);
            where.and().between("sr", arg2, arg3);
            this.querys.put("playersByRoleSR", new QueryPair<>(queryBuilder.prepare(), arg1, arg2, arg3));
            queryBuilder.reset();
        }
        // Select players by all
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            final Where<Player, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            final SelectArg arg2 = new SelectArg();
            final SelectArg arg3 = new SelectArg();
            final SelectArg arg4 = new SelectArg();
            where.eq("role", arg1);
            where.and().between("sr", arg2, arg3);
            where.and().like("description", arg4);
            this.querys.put("playersAll", new QueryPair<>(queryBuilder.prepare(), arg1, arg2, arg3, arg4));
            queryBuilder.reset();
        }
        // Select all expired players
        {
            final QueryBuilder<Player, String> queryBuilder = this.playerDao.queryBuilder();
            final Where<Player, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            where.le("lastUpdate", arg1);
            this.querys.put("playersExpired", new QueryPair<>(queryBuilder.prepare(), arg1));
            queryBuilder.reset();
        }

        // Select all fitting searches
        {
            final QueryBuilder<Search, String> queryBuilder = this.searchDao.queryBuilder();
            final Where<Search, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            where.eq("role", arg1);
            where.or().eq("role", Role.ANY);
            this.querys.put("searchQuery", new QueryPair<>(queryBuilder.prepare(), arg1));
            queryBuilder.reset();
        }
        // Select all searches by user
        {
            final QueryBuilder<Search, String> queryBuilder = this.searchDao.queryBuilder();
            final Where<Search, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            where.eq("userID", arg1);
            this.querys.put("searchID", new QueryPair<>(queryBuilder.prepare(), arg1));
            queryBuilder.reset();
        }
        // Select all expired searches
        {
            final QueryBuilder<Search, String> queryBuilder = this.searchDao.queryBuilder();
            final Where<Search, String> where = queryBuilder.where();
            final SelectArg arg1 = new SelectArg();
            where.le("lastUpdate", arg1);
            this.querys.put("searchesExpired", new QueryPair<>(queryBuilder.prepare(), arg1));
            queryBuilder.reset();
        }
    }

    /**
     * Return instant of singleton.
     *
     * @return Instant of singleton
     */
    public static DatabaseManager inst() {
        return inst;
    }

    /**
     * Initialization of singleton.
     *
     * @param connectionSource
     *            Database connection
     * @throws IllegalArgumentException
     *             Thrown if either connectionSource is not or
     *             {@link DatabaseManager#inst} is set
     * @throws SQLException
     */
    public static void init(final ConnectionSource connectionSource) throws IllegalArgumentException, SQLException {
        if (connectionSource == null || inst != null) throw new IllegalArgumentException();
        inst = new DatabaseManager(connectionSource);
    }

    /**
     * Return database request according to given form and values.
     *
     * @param form
     *            Pre-created form to use
     * @param args
     *            Arguments for said form
     * @return List of players fitting given form and arguments
     */
    public List<Player> getPlayers(final String form, final Object... args) {
        final QueryPair<Player> temp = this.querys.get(form);

        for (int i = 0; i < temp.getRight().length; i++) {
            temp.getRight()[i].setValue(i < args.length ? args[i] : null);
        }

        try {
            return this.playerDao.query(temp.getLeft());
        } catch (final SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Return list of {@link Player Players} applicable to given {@link Search}.
     *
     * @param search
     *            Search matches requested.
     * @return List of fitting players
     */
    public List<Player> getPlayers(final Search search) {
        final List<Object> values = new ArrayList<>();
        String form;
        if (search.getRole() == Role.ANY) {
            form = "playersBySR";
        } else {
            form = "playersByRoleSR";
            values.add(search.getRole());
        }

        if (search.getSr() == -1) {
            search.setSr(0);
            search.setRange(5000);
            form = search.getRole() == Role.ANY ? "allPlayers" : "playersByRole";
        } else {
            values.add(search.getMinSr());
            values.add(search.getMaxSr());
        }
        return this.getPlayers(form, values.toArray());
    }

    /**
     * Return {@link Search Searches} suiting given player.
     *
     * @param player
     *            Player to fit
     * @return List of fitting Searches
     */
    public List<Search> getSearches(final Player player) {
        final QueryPair<Search> searchQuery = this.querys.get("searchQuery");
        searchQuery.getRight()[0].setValue(player.getRole());
        try {
            return this.searchDao //
                    .query(searchQuery.getLeft()) //
                    .stream() //
                    .filter(s -> s.isInRange(player.getSr())) //
                    .collect(Collectors.toList());
        } catch (final SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Player getPlayerByID(final String id) throws SQLException {
        return this.playerDao.createIfNotExists(new Player(id));
    }

    /**
     * Check database for expiring data-sets, deleting (10 days) or returning (7
     * days) untouched sets.
     *
     * @return Data-sets that will be deleted soon, if untouched
     */
    public List<? extends Expirable> getExpiredEntrys() {
        try {
            final List<Expirable> returnV = new ArrayList<>();
            {
                final QueryPair<Search> searchExp = this.querys.get("searchesExpired");
                searchExp.getRight()[0].setValue(Date.valueOf(LocalDate.now().minusDays(7).toString()));
                final List<Search> expiredS = this.searchDao.query(searchExp.getLeft());
                expiredS.stream().filter(s -> s.isExpired() == 2).forEach(s -> {
                    try {
                        this.searchDao.delete(s);
                    } catch (final SQLException e) {}
                });
                returnV.addAll(this.searchDao.query(searchExp.getLeft()));
            }
            {
                final QueryPair<Player> searchExp = this.querys.get("playersExpired");
                searchExp.getRight()[0].setValue(Date.valueOf(LocalDate.now().minusDays(7).toString()));

                final List<Player> expiredP = this.playerDao.query(searchExp.getLeft());
                expiredP.stream().filter(p -> p.isExpired() == 2).forEach(p -> {
                    try {
                        this.playerDao.delete(p);
                    } catch (final SQLException e) {}
                });
                returnV.addAll(this.playerDao.query(searchExp.getLeft()));
            }
            return returnV;
        } catch (final SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Delete {@link Player}-Profile of player with given id.
     *
     * @param id
     *            Discord-ID of user to remove from the database
     * @throws SQLException
     */
    public void deletePlayer(final String id) throws SQLException {
        this.playerDao.deleteById(id);
    }

    /**
     * Delete all {@link Search searches} created by given user id.
     *
     * @param id
     *            Discord-ID of to delete searches
     * @throws SQLException
     */
    public void deleteSearches(final String id) throws SQLException {
        final QueryPair<Search> temp = this.querys.get("searchID");
        temp.getRight()[0].setValue(id);
        this.searchDao.query(temp.getLeft()).forEach(s -> {
            try {
                this.searchDao.delete(s);
            } catch (final SQLException e) {}
        });
    }

    /**
     * Create {@link Player} or update if exists.
     *
     * @param player
     *            Player to create/update
     * @return If succeeded
     */
    public boolean updatePlayer(final Player player) {
        try {
            player.setLastUpdate(LocalDate.now());
            this.playerDao.createOrUpdate(player);
        } catch (final SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Save a {@link Search} in the database.
     *
     * @param search
     *            Search to save
     * @throws SQLException
     */
    public void createSearch(final Search search) throws SQLException {
        this.searchDao.createIfNotExists(search);
    }

    /**
     * Refresh all update-dates associated with given Discord-ID.
     * 
     * @param id
     *            Discord-ID to update
     */
    public void update(final String id) {
        try {
            final Player p = this.playerDao.queryForId(id);
            if (p != null) {
                this.playerDao.update(p);
            }
            final QueryPair<Search> temp = this.querys.get("searchID");
            temp.getRight()[0].setValue(id);

            for (final Search s : this.searchDao.query(temp.getLeft())) {
                s.setLastUpdate(LocalDate.now());
                this.searchDao.update(s);
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }
}
