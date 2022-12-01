package pt.isec.pd.ticketline.src.model.data;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper implements Serializable {
    private Integer id;
    private String operation;
    private String table;
    private ArrayList<ArrayList<String>> seatParams;
    private ArrayList<String> insertParams;
    private HashMap<String, String> updateParams;
    private Socket socketClient;


    public void reset(){
        this.id = null;
        this.operation = null;
        this.table = null;
        this.seatParams = null;
        this.insertParams = null;
        this.updateParams = null;
        this.socketClient = null;
    }

    public void setSocketClient(Socket socket){
        this.socketClient = socket;
    }

    public Socket getSocketClient(){
        return this.socketClient;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public ArrayList<ArrayList<String>> getSeatParams() {
        return seatParams;
    }

    public void setSeatParams(ArrayList<ArrayList<String>> seatParams) {
        this.seatParams = seatParams;
    }

    public ArrayList<String> getInsertParams() {
        return insertParams;
    }

    public void setInsertParams(ArrayList<String> insertParams) {
        this.insertParams = insertParams;
    }

    public HashMap<String, String> getUpdateParams() {
        return updateParams;
    }

    public void setUpdateParams(HashMap<String, String> updateParams) {
        this.updateParams = updateParams;
    }
}