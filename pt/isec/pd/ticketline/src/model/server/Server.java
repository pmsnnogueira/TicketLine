package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ExecutorSendHeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ServerLifeCheck;
import pt.isec.pd.ticketline.src.ui.ServerUI;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";
    private final Data data;
    private final String DBDirectory;
    private volatile boolean available;
    private int numberOfConnections;
    private HeartBeat dbCopyHeartBeat;
    private HeartBeat heartBeat;
    private HeartBeat hbWithHighestVersion;
    private HeartBeatReceiver hbh;
    private DataBaseHandler dbHandler;
    private DBHelper dbHelper;
    private int tcpPort;
    private ServerInit si;
    private DatabaseProvider dbProv;

    private boolean serverInitContinue;
    private boolean handleDB;

    private MulticastSocket mcs;

    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;

    public static void main(String[] args)
    {
        ServerUI serverUI = null;
        try{
            ModelManager modelManager = new ModelManager(Integer.parseInt(args[0]), args[1]);
            serverUI = new ServerUI(modelManager);
        }catch (SQLException | IOException | InterruptedException e){
            e.printStackTrace();
        }

        assert serverUI != null;
        serverUI.start();
    }

    public Server(int port, String DBDirectory) throws SQLException, IOException, InterruptedException {
        this.available = true;
        this.numberOfConnections = 0;
        this.dbCopyHeartBeat = null;
        this.DBDirectory = DBDirectory;
        this.tcpPort = port;
        this.serverInitContinue = true;
        this.mcs = new MulticastSocket(multicastPort);
        this.ipGroup = InetAddress.getByName(ipMulticast);
        this.sa = new InetSocketAddress(ipGroup, multicastPort);
        this.ni = NetworkInterface.getByIndex(0);
        this.mcs.joinGroup(sa, ni);

        this.data = new Data(this.mcs);
        //START SERVER

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // server initiaton phase
         si = new ServerInit();
         si.start();
         si.join(30000);
         this.serverInitContinue = false;

         transferDatabase(dbCopyHeartBeat);

        //start thread to handle the DB operations
        this.hbWithHighestVersion = null;
        this.handleDB = true;
        this.dbHelper = new DBHelper();

         this.dbHandler = new DataBaseHandler();
         this.dbHandler.start();


        heartBeat = new HeartBeat(port, available, this.data.getDatabaseVersion(),
                numberOfConnections, DBDirectory, "127.0.0.1");

        this.data.setServerHeartBeat(heartBeat);

        //Every 10 seconds, the server will send a heart beat through multicast
        //to every other on-line server
        scheduler.scheduleAtFixedRate(new ExecutorSendHeartBeat(heartBeat, mcs),
                0, 10, TimeUnit.SECONDS);
        //Every 35 seconds, the server will check if there is any server who hasn't
        scheduler.scheduleAtFixedRate(new ServerLifeCheck(this.data), 0, 35, TimeUnit.SECONDS);

        //start thread to receive the heartbeats
        hbh = new HeartBeatReceiver(mcs);
        hbh.start();

        //start database prpovider thread to pro
        dbProv = new DatabaseProvider();
        dbProv.start();
    }

    public String listUsers(Integer userID){
        this.dbHelper.setId(userID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("user");
        this.dbHandler.setHasNewDBRequest();
        return "";
    }

    public String listShows(Integer showID){
        this.dbHelper.setId(showID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("show");
        this.dbHandler.setHasNewDBRequest();
        return "";
    }

    public String listReservations(Integer reservationID){
        this.dbHelper.setId(reservationID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("reservation");
        this.dbHandler.setHasNewDBRequest();
        return "";
    }

    public String listSeats(Integer seatID){
        this.dbHelper.setId(seatID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("seat");
        this.dbHandler.setHasNewDBRequest();
        return "";
    }

    public void insertShow(){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("show");
        this.dbHandler.setHasNewDBRequest();
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("seat");
        this.dbHelper.setSeatParams(parameters);
        this.dbHelper.setId(numShow);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean insertReservation(ArrayList<String> parameters){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setInsertParams(parameters);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean insertUser(ArrayList<String> parameters){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("user");
        this.dbHelper.setInsertParams(parameters);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean deleteShow(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("show");
        this.dbHelper.setId(id);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean deleteReservations(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setId(id);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean deleteSeat(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("seat");
        this.dbHelper.setId(id);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean deleteUsers(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("user");
        this.dbHelper.setId(id);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("show");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("seat");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("user");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
        this.dbHandler.setHasNewDBRequest();
        return true;
    }

    public void transferDatabase(HeartBeat dbHeartbeat){
        if((new File(DBDirectory + "/PD-2022-23-TP-" + tcpPort + ".db")).exists()){
            if (dbHeartbeat == null){
                return;
            }
            if(this.data.testDatabaseVersion(DBDirectory, tcpPort) >= dbHeartbeat.getDatabaseVersion()){
                return;
            }
        }

        if(dbHeartbeat == null){
            return;
        }

        try {
            Socket socket = new Socket(dbHeartbeat.getIp(), dbHeartbeat.getPortTcp());
            File file = new File(DBDirectory + "/PD-2022-23-TP-" + tcpPort + ".db");
            FileOutputStream fo = new FileOutputStream(file);
            byte[] buffer = new byte[512];
            InputStream is = socket.getInputStream();
            int readBytes=0;

            do
            {
                readBytes = is.read(buffer);
                if(readBytes > -1)
                    fo.write(buffer, 0, readBytes);
            }while(readBytes > 0);
            socket.close();
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }  
    }

    public void updateDBVersion(){
        this.heartBeat.setDatabaseVersion(this.data.getDatabaseVersion());
    }

    public void closeServer() throws InterruptedException, IOException, SQLException {
        this.handleDB = false;
        this.dbHandler.join();
        this.dbHandler.interrupt();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(heartBeat);
        byte[] buffer = baos.toByteArray();
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                InetAddress.getByName("239.39.39.39"), 4004);
        mcs.send(dp);

        heartBeat.setAvailable(false);
        hbh.join(10000);
        hbh.interrupt();

        mcs.leaveGroup(sa, ni);
        mcs.close();
        this.data.closeDB();
    }

    private void updateDB(HeartBeat hBeat) {
        this.data.processNewQuerie(hBeat.getMostRecentQuery());
        this.heartBeat.resetMostRecentQuery();
    }

    public String listAllAvailableServers() {
        return this.data.listAllAvailableServers();
    }

    class DataBaseHandler extends Thread{
        private boolean hasNewDBRequest;
        public DataBaseHandler(){
            this.hasNewDBRequest = false;
        }

        public void setHasNewDBRequest() {
            this.hasNewDBRequest = true;
        }

        @Override
        public void run() {
            //Connect to DB
            if(!data.connectToDB(tcpPort, DBDirectory)){
                return;
            }

            while(handleDB){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(hbWithHighestVersion != null){
                    updateDB(hbWithHighestVersion);
                    hbWithHighestVersion = null;
                    updateDBVersion();
                }

                if (this.hasNewDBRequest){
                    switch (dbHelper.getOperation()){
                        case "INSERT"->{
                            switch (dbHelper.getTable()){
                                case "show" ->{
                                    System.out.println("Vou tentar");
                                    data.addShow();
                                }
                                case "seat" ->{
                                    data.insertSeat(dbHelper.getSeatParams(), dbHelper.getId());
                                }
                                case "reservation" ->{
                                    data.insertReservation(dbHelper.getInsertParams());
                                }
                                case "user" ->{
                                    data.insertUser(dbHelper.getInsertParams());
                                }
                            }
                        }
                        case "SELECT"->{
                            switch (dbHelper.getTable()){
                                case "show" ->{
                                    System.out.println(data.listShows(dbHelper.getId()));
                                }
                                case "seat" ->{
                                    System.out.println(data.listSeats(dbHelper.getId()));
                                }
                                case "reservation" ->{
                                    System.out.println(data.listReservations(dbHelper.getId()));
                                }
                                case "user" ->{
                                    System.out.println(data.listUsers(dbHelper.getId()));
                                }
                            }
                        }
                        case "UPDATE"->{
                            switch (dbHelper.getTable()){
                                case "show" ->{
                                    data.updateShows(dbHelper.getId(),dbHelper.getUpdateParams());
                                }
                                case "seat" ->{
                                    data.updateSeats(dbHelper.getId(), dbHelper.getUpdateParams());
                                }
                                case "reservation" ->{
                                    data.updateReservation(dbHelper.getId(), dbHelper.getUpdateParams());
                                }
                                case "user" ->{
                                    data.updateUser(dbHelper.getId(), dbHelper.getUpdateParams());
                                }
                            }
                        }
                        case "DELETE"->{
                            switch (dbHelper.getTable()){
                                case "show" ->{
                                    data.deleteShow(dbHelper.getId());
                                }
                                case "seat" ->{
                                    data.deleteSeat(dbHelper.getId());
                                }
                                case "reservation" ->{
                                    data.deleteReservations(dbHelper.getId());
                                }
                                case "user" ->{
                                    data.deleteUsers(dbHelper.getId());
                                }
                            }
                        }
                    }

                    if(!dbHelper.getOperation().equals("SELECT")){
                        updateDBVersion();
                    }
                    dbHelper.reset();
                    hasNewDBRequest = false;
                }
            }
        }
    }


    class HeartBeatReceiver extends Thread{
        private MulticastSocket mcs;

        public HeartBeatReceiver(MulticastSocket mcs){
            this.mcs = mcs;
        }

        @Override
        public void run() {
            while(true)
            {
                if (mcs.isClosed()){
                    break;
                }
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[1024], 1024);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat hBeat = (HeartBeat)ois.readObject();

                        data.processANewHeartBeat(hBeat);

                        if(hBeat.getDatabaseVersion() > heartBeat.getDatabaseVersion()
                                && hBeat.getPortTcp() != tcpPort){
                            hbWithHighestVersion = hBeat;
                        }
                    }
                    catch(ClassCastException | ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }

                    data.checkForServerDeath();

                }catch (IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    class ServerInit extends Thread{
        @Override
        public void run() {
            HeartBeat heartBeat;
            while(serverInitContinue)
            {
                if (mcs.isClosed()){
                    break;
                }
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[512], 512);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {

                        heartBeat = (HeartBeat)ois.readObject();

                        if(heartBeat.getPortTcp() == tcpPort){
                            continue;
                        }
                    
                        if(dbCopyHeartBeat == null){
                            dbCopyHeartBeat = heartBeat;
                            continue;
                        }
                        if(heartBeat.getDatabaseVersion() > dbCopyHeartBeat.getDatabaseVersion()){
                            System.out.println(heartBeat.getMostRecentQuery());
                            dbCopyHeartBeat = heartBeat;
                        }
                    }
                    catch(ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }
                }catch (IOException e){
                    break;
                }
            }
        }
    }

    class DatabaseProvider extends Thread{
        @Override
        public void run() {
            ServerSocket serverSocket;
            Socket socket;
            FileInputStream fi;
            while(true)
            {
                try {
                    serverSocket = new ServerSocket(tcpPort);
                    socket = serverSocket.accept();
                    OutputStream os = socket.getOutputStream();
                    byte[] buffer = new byte[512];
                    int readBytes = 0;
                    fi = new FileInputStream(DBDirectory + "/PD-2022-23-TP-" + tcpPort + ".db");
    
                    do
                    {
                        readBytes = fi.read(buffer);
                        if(readBytes == -1)
                            break;
                        os.write(buffer, 0, readBytes);
                    }while(readBytes > 0);
    
                } catch (IOException e) {
                    return;
                }
                try {
                    serverSocket.close();
                    socket.close();
                    fi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            

        }
    }
}
