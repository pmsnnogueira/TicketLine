package pt.isec.pd.ticketline.src.model.data;

import pt.isec.pd.ticketline.src.resources.ResourcesManager;

import java.sql.SQLException;

public class Data {
    private ResourcesManager resourcesManager;

    public Data() throws SQLException {
        this.resourcesManager = new ResourcesManager();
    }

    public String listUsers(Integer userID) throws SQLException{
        return this.resourcesManager.listUsers(userID);
    }

    public String listShows(Integer showID) throws SQLException{
        return this.resourcesManager.listShows(showID);
    }

    public String listReservations(Integer reservationID) throws SQLException{
        return this.resourcesManager.listReservations(reservationID);
    }

    public String listSeats(Integer seatID) throws SQLException{
        return this.resourcesManager.listSeats(seatID);
    }

    public void closeDB() throws SQLException{
        this.resourcesManager.closeDB();
    }
}
