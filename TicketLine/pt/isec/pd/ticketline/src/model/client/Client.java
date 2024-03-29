package pt.isec.pd.ticketline.src.model.client;

import pt.isec.pd.ticketline.src.model.data.DBHelper;
import pt.isec.pd.ticketline.src.ui.ClientUI;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Client {
    public static void main(String[] args) {
        ClientUI clientUI = null;
        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            clientUI = new ClientUI(client);
        } catch (IOException e) {
            System.out.println("Could not initiate a client");
            return;
        }

        clientUI.start();
    }

    private static final String SELECT = "SELECT";
    private static final String INSERT = "INSERT";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";
    private static final String USER = "user";
    private static final String SHOW = "show";
    private static final String SEAT = "seat";
    private static final String RESERVATION = "reservation";

    private static final String RESERVATION_SEAT = "reservation_seat";
    private static final String CONFIRMED = "CONFIRMED";

    public String serverIP;
    public int serverPort;
    public boolean CIHandle;
    public ArrayList<String> servers;
    ConnectToServer sr;
    public AtomicReference<Boolean> srHandle;
    public AtomicReference<Boolean> confirmHandle;
    public AtomicReference<Boolean> hasNewRequest;
    public DBHelper dbHelper;

    public AtomicInteger indexSV;
    public AtomicReference<String> requestResult;
    public int clientID;
    private AtomicReference<Boolean> isSocketNull;

    private String username;

    public Client(String serverIP, int serverPort) throws IOException {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.CIHandle = true;
        this.srHandle = new AtomicReference<>(true);
        this.confirmHandle = new AtomicReference<>(false);
        this.hasNewRequest = new AtomicReference<>(false);
        this.indexSV = new AtomicInteger(0);
        this.dbHelper = null;
        this.requestResult = new AtomicReference<>("");

        this.servers = new ArrayList<>();

        this.isSocketNull = new AtomicReference<>(true);

        if (!clientInit()) {
            throw new IOException();
        }

        connectToServer();
    }

    public boolean clientInit() {
        DatagramSocket socket;
        InetAddress ip;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            ip = InetAddress.getByName(serverIP);
        } catch (IOException e) {
            System.out.println("Error");
            return false;
        }
        String message = "CONNECTION";
        DatagramPacket packetSent = new DatagramPacket(message.getBytes(), message.getBytes().length, ip, serverPort);

        try {
            socket.send(packetSent);
        } catch (IOException e) {
            return false;
        }

        DatagramPacket packetReceived = new DatagramPacket(new byte[256], 256);

        try {
            socket.receive(packetReceived);
        } catch (IOException e) {
            System.out.println("Error");

            return false;
        }

        String messageReceived = new String(packetReceived.getData(), 0, packetReceived.getLength());

        if(messageReceived.isEmpty()){
            return false;
        }

        String[] strings = messageReceived.split("\\|");

        servers.clear();
        servers.addAll(Arrays.asList(strings));
        System.out.println(servers);
        socket.close();
        return true;
    }

    public DBHelper addDBHelper(String operation, String table, ArrayList<String> insertParams, int id , ArrayList<String> userLogin) {
        DBHelper dbHelper = new DBHelper();
        if (operation.equals(INSERT)) {
            if (table.equals(USER)) {
                insertParams.add("0");
                insertParams.add("0");
                insertUser(dbHelper,insertParams);
                return dbHelper;
            }
            if (table.equals(SHOW)) {
                insertShow(dbHelper);
                return dbHelper;
            }
            if (table.equals(RESERVATION)) {
                insertReservation(dbHelper,insertParams);
                return dbHelper;
            }
            if (table.equals(RESERVATION_SEAT)) {
                insertReservationSeat(dbHelper,insertParams);
                return dbHelper;
            }
        }
        if(operation.equals(SELECT)){
            if(table.equals(USER)){
                if(userLogin != null){
                    verifyUserLogin(dbHelper,userLogin);
                    return dbHelper;
                }
                listUsers(dbHelper , id);
                return dbHelper;
            }
            if(table.equals(SHOW)){
                listShows(dbHelper , insertParams);
                return dbHelper;
            }
            if(table.equals(RESERVATION)){
                listReservations(dbHelper , id);
                return dbHelper;
            }
            if(table.equals(SEAT)){
                listSeats(dbHelper , id);
                return dbHelper;
            }
        }
        return null;
    }

    public DBHelper addDBHelper(int id, String operation, String table, HashMap<String,String> newData){
        DBHelper dbHelper = new DBHelper();
        dbHelper.setOperation(operation);
        dbHelper.setTable(table);
        dbHelper.setId(id);
        dbHelper.setUpdateParams(newData);
        return dbHelper;
    }
    //Function to send information with new queries
    public DBHelper addDBHelper(int id, String operation, String table, int option, ArrayList<String> parameters){
        DBHelper dbHelper = new DBHelper();
        dbHelper.setOperation(operation);
        dbHelper.setTable(table);
        dbHelper.setId(id);
        dbHelper.setOption(option);
        if(parameters != null)
            dbHelper.setInsertParams(parameters);
        return dbHelper;
    }

    public String waitToReceiveResultRequest(){
        while(true){
            if(!requestResult.get().equals("")){
                return requestResult.get();
            }
        }
    }

    public void connectToServer(){
        Socket socketSr;
        OutputStream os = null;
        InputStream is = null;

        //parse information about the servers
        for (String sv : servers){
            String[] s = sv.split("-");
            try {
                socketSr = new Socket(s[0], Integer.parseInt(s[1]));
                os = socketSr.getOutputStream();
                is = socketSr.getInputStream();

                String client = "CLIENT";
                os.write(client.getBytes(), 0, client.length());

                this.sr = new ConnectToServer(socketSr, os, is);
                sr.start();
                return;
            } catch (IOException e) {
                indexSV.set(indexSV.get()+1 > servers.size()-1? 0 : indexSV.get()+1);
                continue;
            }
        }
    }

    class ConnectToServer extends Thread{
        private Socket socketSr;
        private OutputStream os;
        private InputStream is;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;

        public ConnectToServer(Socket socketSr, OutputStream os, InputStream is){
            this.socketSr = socketSr;
            this.is = is;
            this.os = os;
            this.oos = null;
            this.ois = null;
        }

        @Override
        public void run() {
            while(srHandle.get()) {
                if(hasNewRequest.get()){
                    requestResult.set("");
                    try {
                        String n = "NEW REQUEST";
                        os.write(n.getBytes(), 0, n.length());

                        byte[] m = new byte[512];
                        int nBytes = is.read(m);
                        String msgReceived = new String(m, 0, nBytes);

                        if(msgReceived.equals("CONFIRMED")){
                            if(ois == null){
                                ois = new ObjectInputStream(socketSr.getInputStream());
                            }
                            //get updated list of servers
                            String newServers = (String) ois.readObject();
                            String[] strings = newServers.split("\\|");
                            servers.clear();
                            servers.addAll(Arrays.asList(strings));
                            System.out.println(servers);

                            //reset index
                            indexSV.set(0);

                            if(oos == null){
                                oos = new ObjectOutputStream(socketSr.getOutputStream());
                            }

                            oos.writeObject(dbHelper);

                            if(dbHelper.isLogout()){
                                return;
                            }

                            requestResult.set((String) ois.readObject());
                        }
                        if(msgReceived.equals("SERVER IS UPDATING - PLEASE TRY AGAIN")){
                            os.close();
                            is.close();
                            socketSr.close();

                            //if the server fails to receive the client request
                            //and the client has sent a request to every
                            if(indexSV.get()==servers.size()){
                                requestResult.set(msgReceived);
                                indexSV.set(0);
                                dbHelper = null;
                                hasNewRequest.set(false);
                                connectToServer();
                                return;
                            }
                            hasNewRequest.set(false);
                            indexSV.set(indexSV.get()+1);
                            connectToServer();
                            return;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        indexSV.set(0);
                        hasNewRequest.set(false);
                        connectToServer();
                        return;
                    }

                    hasNewRequest.set(false);
                }
            }
        }
    }


    public void createDBHelper(String operation, String table, ArrayList<String> insertParams , int id,  ArrayList<String> userLogin) {
        dbHelper = addDBHelper(operation, table, insertParams, id , userLogin);
        hasNewRequest.set(true);
    }

    public void createDBHelper(int id, String operation, String table, HashMap<String,String> newData){
        dbHelper = addDBHelper(id, operation, table, newData);
        hasNewRequest.set(true);
    }

    //Function to send new queries SELECT
    public void createDBHelper(int id,String operation, String table , int option,String parameters) {
            ArrayList<String> aux = null;
            if(parameters != null) {
                aux = new ArrayList<>();
                aux.add(parameters);
            }

            dbHelper = addDBHelper(id,operation, table,  option, aux);
            hasNewRequest.set(true);
    }
    public boolean insertUser(DBHelper dbHelper,ArrayList<String> parameters){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(USER);
        dbHelper.setInsertParams(parameters);
        return true;
    }

    public void insertShow(DBHelper dbHelper){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(SHOW);
    }

    public boolean insertReservation(DBHelper dbHelper ,ArrayList<String> parameters){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(RESERVATION);
        dbHelper.setInsertParams(parameters);
        return true;
    }

    public boolean insertReservationSeat(DBHelper dbHelper ,ArrayList<String> parameters){
        dbHelper.setOperation(INSERT);
        dbHelper.setTable(RESERVATION_SEAT);
        dbHelper.setInsertParams(parameters);
        return true;
    }

    public String listUsers(DBHelper dbHelper,Integer userID){
        dbHelper.setId(userID == -1 ? null : userID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(USER);
        return "";
    }

    public String listShows(DBHelper dbHelper,ArrayList<String> parameters){
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(SHOW);
        dbHelper.setInsertParams(parameters);
        return "";
    }

    public String listReservations(DBHelper dbHelper,Integer reservationID){
        dbHelper.setId(reservationID == -1 ? null : reservationID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(RESERVATION);
        return "";
    }

    public String listSeats(DBHelper dbHelper, Integer showID){
        dbHelper.setId(showID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(SEAT);

        return "";
    }
    public String verifyUserLogin(DBHelper dbHelper ,ArrayList<String> parameters){
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(USER);
        dbHelper.setVerifyUsername(parameters);
        this.username = parameters.get(0);
        return "";
    }

    public void closeClient(){
        dbHelper = new DBHelper();
        dbHelper.setLogout(username);
        hasNewRequest.set(true);
/*        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {

        }*/
        srHandle.set(false);
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getUsername() {
        return username;
    }
}
