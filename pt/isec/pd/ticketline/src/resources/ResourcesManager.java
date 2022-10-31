package pt.isec.pd.ticketline.src.resources;

import pt.isec.pd.ticketline.src.resources.db.DBManager;

import java.sql.SQLException;
import java.util.ArrayList;

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

    public boolean insertShow(ArrayList<String> parameters) {
        return this.dbManager.insertShow(parameters);
    }

    public boolean insertSeat(ArrayList<String> parameters){
        return this.dbManager.insertSeat(parameters);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.dbManager.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.dbManager.insertUser(parameters);
    }

    public void closeDB() throws SQLException {
        this.dbManager.close();
    }
}
