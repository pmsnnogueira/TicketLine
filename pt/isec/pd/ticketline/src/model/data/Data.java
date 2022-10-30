package pt.isec.pd.ticketline.src.model.data;

import pt.isec.pd.ticketline.src.resources.ResourcesManager;
import pt.isec.pd.ticketline.src.resources.files.FileOpener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Data {
    private ResourcesManager resourcesManager;
    private ArrayList<Show> shows;

    public Data() throws SQLException {
        this.resourcesManager = new ResourcesManager();
        this.shows = new ArrayList<>();
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

    public boolean insertShow(ArrayList<String> parameters){
        return this.resourcesManager.insertShow(parameters);
    }

    public boolean insertSeat(ArrayList<String> parameters){
        return this.resourcesManager.insertSeat(parameters);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.resourcesManager.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.resourcesManager.insertUser(parameters);
    }

    public void closeDB() throws SQLException{
        this.resourcesManager.closeDB();
    }

    public void addShow(ArrayList<String> information)
    {
    }
}
