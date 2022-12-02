package pt.isec.pd.ticketline.src.model.client;

import pt.isec.pd.ticketline.src.model.data.DBHelper;
import pt.isec.pd.ticketline.src.ui.ClientUI;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Client {

    private static final String SELECT = "SELECT";
    private static final String INSERT = "INSERT";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    private static final String USER = "user";
    private static final String SHOW = "show";
    private static final String SEAT = "seat";
    private static final String RESERVATION = "reservation";

    private static final String CONFIRMED = "CONFIRMED";
    public static void main(String[] args) {
        ClientUI clientUI = null;
        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            clientUI = new ClientUI(client);
        } catch (IOException e) {
            System.out.println("Could not create a client (ERROR:" + e + ")");
            return;
        }

        clientUI.start();
    }

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

        if (!clientInit()) {
            throw new IOException();
        }

        this.sr = new ConnectToServer();
        sr.start();
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
            System.out.println("Error");

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
        if (operation.equals("INSERT")) {
            if (table.equals("user")) {
                insertUser(dbHelper,insertParams);
                return dbHelper;
            }
            if (table.equals("show")) {
                insertShow(dbHelper);
                return dbHelper;
            }
            if (table.equals("reservation")) {
                insertReservation(dbHelper,insertParams);
                return dbHelper;
            }
        }
        if(operation.equals("SELECT")){
            if(table.equals("user")){
                if(userLogin != null){
                    verifyUserLogin(dbHelper,userLogin);
                    return dbHelper;
                }
                listUsers(dbHelper , id);
                return dbHelper;
            }
            if(table.equals("show")){
                listShows(dbHelper , id);
                return dbHelper;
            }
            if(table.equals("reservation")){
                listReservations(dbHelper , id);
                return dbHelper;
            }
        }
        return null;
    }

    public String waitToReceiveResultRequest(){
        while(true){
            if(!requestResult.get().equals("")){
                return requestResult.get();
            }
        }
    }

    class ConnectToServer extends Thread{
        @Override
        public void run() {
            while(srHandle.get()) {
                if(hasNewRequest.get()){
                    requestResult.set("");
                    try {
                        String[] s = servers.get(indexSV.get()).split("-");
                        Socket socketSr = null;
                        try {
                            socketSr = new Socket(s[0], Integer.parseInt(s[1]));
                        } catch (IOException e) {
                            indexSV.set(indexSV.get()+1 > servers.size()-1? 0 : indexSV.get()+1);
                            continue;
                        }

                        OutputStream os = socketSr.getOutputStream();
                        InputStream is = socketSr.getInputStream();

                        String client = "CLIENT";
                        os.write(client.getBytes(), 0, client.length());

                        byte[] m = new byte[512];
                        int nBytes = is.read(m);
                        String msgReceived = new String(m, 0, nBytes);



                        if(msgReceived.equals("CONFIRMED")){
                            ObjectOutputStream oos = new ObjectOutputStream(socketSr.getOutputStream());

                            oos.writeObject(dbHelper);

                            ObjectInputStream ois = new ObjectInputStream(socketSr.getInputStream());

                            requestResult.set((String) ois.readObject());

                            oos.close();
                            ois.close();
                        }
                        if(msgReceived.equals("SERVER IS UPDATING - PLEASE TRY AGAIN")){
                            requestResult.set("SERVER IS UPDATING - PLEASE TRY AGAIN");
                        }

                        os.close();
                        is.close();
                        socketSr.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
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

    public String listUsers(DBHelper dbHelper,Integer userID){
        dbHelper.setId(userID == -1 ? null : userID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(USER);
        return "";
    }

    public String listShows(DBHelper dbHelper,Integer showID){
        dbHelper.setId(showID == -1 ? null : showID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(SHOW);
        return "";
    }

    public String listReservations(DBHelper dbHelper,Integer reservationID){
        dbHelper.setId(reservationID == -1 ? null : reservationID);
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(RESERVATION);
        return "";
    }

    public String verifyUserLogin(DBHelper dbHelper ,ArrayList<String> parameters){
        dbHelper.setOperation(SELECT);
        dbHelper.setTable(USER);
        dbHelper.setVerifyUsername(parameters);
        return "";
    }

}
