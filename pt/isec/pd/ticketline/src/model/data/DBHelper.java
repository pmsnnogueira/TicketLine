package pt.isec.pd.ticketline.src.model.data;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DBHelper implements Serializable {

    private Integer id;
    private String operation;
    private String table;

    private Integer option;
    private ArrayList<ArrayList<String>> seatParams;
    private ArrayList<String> insertParams;
    private HashMap<String, String> updateParams;
    private ArrayList<String> verifyUsername;
    private boolean alreadyProcessed;
    
    private AtomicReference<String> requestResult;

    public DBHelper(){
        this.requestResult = new AtomicReference<>("");
    }

    public void reset(){
        this.id = null;
        this.operation = null;
        this.table = null;
        this.option = null;
        this.seatParams = null;
        this.insertParams = null;
        this.updateParams = null;
        this.verifyUsername = null;
        this.alreadyProcessed = false;
        this.requestResult = new AtomicReference<>("");
    }

    public String getRequestResult() {
        return requestResult.get();
    }

    public void setRequestResult(String requestResult) {
        this.requestResult.set(requestResult);
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


    public Integer getOption() {
        return option;
    }

    public void setOption(Integer option) {
        this.option = option;
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

    public void setVerifyUsername(ArrayList<String> parameters) {
        this.verifyUsername = parameters;
    }

    public ArrayList<String> getverifyUsername() {
        return verifyUsername;
    }

    public boolean isAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setAlreadyProcessed(boolean alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }

}
