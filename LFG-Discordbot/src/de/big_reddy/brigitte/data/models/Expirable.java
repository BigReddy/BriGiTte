/**
 *
 */
package de.big_reddy.brigitte.data.models;

import java.time.LocalDate;

/**
 * @author Big_Reddy
 *
 */
public interface Expirable {

    /**
     * @return the userID
     */
    String getUserID();

    /**
     * @return the lastUpdate
     */
    LocalDate getLastUpdate();

    /**
     * @param lastUpdate
     *            the lastUpdate to set
     */
    void setLastUpdate(final LocalDate lastUpdate);

    /**
     * 0: not expired <br>
     * 1: expired <br>
     * 2: free to delete
     *
     * @return status of deprecation
     */
    default int isExpired() {
        int state = 0;
        state += this.getLastUpdate().isBefore(LocalDate.now().minusDays(7)) ? 0 : 1;
        state += this.getLastUpdate().isBefore(LocalDate.now().minusDays(10)) ? 0 : 1;
        return state;
    }
}
