package de.big_reddy.brigitte.data.models;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players")
public class Player implements Expirable {
    @DatabaseField(id = true)
    private String userID;
    @DatabaseField
    private Role role = Role.NONE;
    @DatabaseField
    private int sr;
    @DatabaseField
    private String description;
    @DatabaseField
    private Date lastUpdate = Date.valueOf(LocalDate.now());

    public Player() {}

    public Player(final String userID) {
        this.userID = userID;
    }

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
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return String.format("***Name:*** <@%s>\n***SR:*** %d\n***Role:*** %s", this.userID, this.sr,
                this.role.toString().toUpperCase());
    }
}
