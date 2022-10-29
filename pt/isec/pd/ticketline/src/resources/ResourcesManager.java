package pt.isec.pd.ticketline.src.resources;

import pt.isec.pd.ticketline.src.resources.db.DBManager;

import java.sql.SQLException;

public class ResourcesManager {
    private final DBManager dbManager;

    public ResourcesManager() throws SQLException {
        this.dbManager = new DBManager();
    }

    public String listUsers(Integer userID) throws SQLException {
        return this.dbManager.listUsers(userID);
    }

    public String listShows(Integer showID) throws SQLException{
        return this.dbManager.listShows(showID);
    }

    public String listReservations(Integer reservationID) throws SQLException{
        return this.dbManager.listReservations(reservationID);
    }

    public String listSeats(Integer seatID) throws SQLException{
        return this.dbManager.listSeats(seatID);
    }
}
