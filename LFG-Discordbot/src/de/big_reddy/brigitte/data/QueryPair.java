package de.big_reddy.brigitte.data;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

import net.dv8tion.jda.core.utils.tuple.Pair;

public class QueryPair<T> extends Pair<PreparedQuery<T>, SelectArg[]> {

    /**
     *
     */
    private static final long serialVersionUID = 4086035009714230375L;
    private final PreparedQuery<T> query;
    private final SelectArg[] arg;

    public QueryPair(final PreparedQuery<T> query, final SelectArg... arg) {
        this.query = query;
        this.arg = arg;
    }

    @Override
    public PreparedQuery<T> getLeft() {
        return this.query;
    }

    @Override
    public SelectArg[] getRight() {
        return this.arg;
    }

}
