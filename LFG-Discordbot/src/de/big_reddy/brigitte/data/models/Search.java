package de.big_reddy.brigitte.data.models;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "searches")
public class Search implements Expirable {
    @DatabaseField
    private String userID;
    @DatabaseField
    private Role role = Role.NONE;
    @DatabaseField
    private int sr;
    @DatabaseField
    private int range = 300;
    @DatabaseField
    private Date lastUpdate = Date.valueOf(LocalDate.now());

    /**
     * @return the userID
     */
    @Override
    public String getUserID() {
        return this.userID;
    }

    /**
     * @param userID
     *            the userID to set
     */
    public void setUserID(final String userID) {
        this.userID = userID;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return this.role;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(final Role role) {
        this.role = role;
    }

    /**
     * @return the sr
     */
    public int getSr() {
        return this.sr;
    }

    /**
     * @param sr
     *            the sr to set
     */
    public void setSr(final int sr) {
        this.sr = sr;
    }

    /**
     * @return the range
     */
    public int getRange() {
        return this.range;
    }

    /**
     * @param range
     *            the range to set
     */
    public void setRange(final int range) {
        this.range = range;
    }

    /**
     * @return the max sr searched
     */
    public int getMaxSr() {
        return this.sr + this.range;
    }

    /**
     * @return the min sr searched
     */
    public int getMinSr() {
        return this.sr - this.range;
    }

    public boolean isInRange(final int sr) {
        return sr >= this.getMinSr() && sr <= this.getMaxSr();
    }

    /**
     * @return the lastUpdate
     */
    @Override
    public LocalDate getLastUpdate() {
        return LocalDate.parse(this.lastUpdate.toString(), DateTimeFormatter.ISO_DATE);
    }

    /**
     * @param lastUpdate
     *            the lastUpdate to set
     */
    @Override
    public void setLastUpdate(final LocalDate lastUpdate) {
        this.lastUpdate = Date.valueOf(lastUpdate);
    }
}
