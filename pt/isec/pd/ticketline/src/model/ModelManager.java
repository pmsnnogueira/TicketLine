package pt.isec.pd.ticketline.src.model;

import pt.isec.pd.ticketline.src.model.data.Data;
import java.sql.SQLException;
import java.util.ArrayList;

public class ModelManager {
    private Data data;

    public ModelManager() throws SQLException {
        this.data = new Data();
    }

    public String listUsers(Integer userID) throws SQLException {
        return this.data.listUsers(userID);
    }

    public String listShows(Integer showID) throws SQLException{
        return this.data.listShows(showID);
    }

    public String listReservations(Integer reservationID) throws SQLException{
        return this.data.listReservations(reservationID);
    }

    public String listSeats(Integer seatID) throws SQLException{
        return this.data.listSeats(seatID);
    }

    public void insertShow(){
        this.data.addShow();
    }

    public boolean insertSeat(ArrayList<String> parameters){
        return this.data.insertSeat(parameters);
    }

    public boolean insertReservation(ArrayList<String> parameters){
        return this.data.insertReservation(parameters);
    }

    public boolean insertUser(ArrayList<String> parameters){
        return this.data.insertUser(parameters);
    }

    public void closeDB() throws SQLException{
        this.data.closeDB();
    }
}
