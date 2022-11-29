package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ExecutorSendHeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ServerLifeCheck;
import pt.isec.pd.ticketline.src.ui.ServerUI;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
    private int serverPort;
    private ServerInit si;
    private TCPHandler dbProv;

    private boolean serverInitContinue;
    private boolean handleDB;

    private MulticastSocket mcs;

    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;
    private ScheduledExecutorService scheduler;
    private boolean HBHandle;
    private UDPHandler ch;
    private boolean UDPHandle;
    private String clientIP;
    private int clientPort;
    private AtomicReference<Boolean> prepare;
    private AtomicReference<Boolean> masterSV;

    private int confirmations;
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
        this.prepare = new AtomicReference<>(false);
        this.masterSV = new AtomicReference<>(false);

        this.available = true;
        this.numberOfConnections = 0;
        this.dbCopyHeartBeat = null;
        this.DBDirectory = DBDirectory;
        this.serverPort = port;
        this.serverInitContinue = true;
        this.mcs = new MulticastSocket(multicastPort);
        this.ipGroup = InetAddress.getByName(ipMulticast);
        this.sa = new InetSocketAddress(ipGroup, multicastPort);
        this.ni = NetworkInterface.getByIndex(0);
        this.mcs.joinGroup(sa, ni);

        this.data = new Data(this.mcs);
        //START SERVER

        this.scheduler = Executors.newScheduledThreadPool(2);

        // server initiaton phase
        si = new ServerInit();
        si.start();
        si.join(30000);
        this.serverInitContinue = false;

        transferDatabase(dbCopyHeartBeat);

        //start thread to handle the DB operations
        this.hbWithHighestVersion = null;
        this.handleDB = true;
        dbHelper = new DBHelper();

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
        this.HBHandle = true;
        hbh = new HeartBeatReceiver(mcs);
        hbh.start();

        //start database prpovider thread to pro
        dbProv = new TCPHandler();
        dbProv.start();

        //start client handler thread
        this.UDPHandle = true;
        this.ch = new UDPHandler();
        this.ch.start();
    }

    public String listUsers(Integer userID){
        dbHelper.setId(userID);
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

    public synchronized void transferDatabase(HeartBeat dbHeartbeat){
        //if the server already has its own DB
        if((new File(DBDirectory + "/PD-2022-23-TP-" + serverPort + ".db")).exists()){
            //and there is no other server running
            if (dbHeartbeat == null){
                //there is no need to do anything
                return;
            }
            //or this server DB has a higher or equal version
            if(this.data.testDatabaseVersion(DBDirectory, serverPort) >= dbHeartbeat.getDatabaseVersion()){
                //there is no need to do anything
                return;
            }
        }

        //if this server does not have its own DB
        //and there are no other servers running
        if(dbHeartbeat == null){
            //there is no need to do anything
            return;
        }

        //if there are other servers running
        //it needs to copy the DB from the server with the highest version
        try {
            Socket socket = new Socket(dbHeartbeat.getIp(), dbHeartbeat.getPortTcp());

            //it needs to send a message indicating that it is a server
            OutputStream os = socket.getOutputStream();
            String str = "SERVER";
            os.write(str.getBytes(), 0, str.length());

            //and prepares to copy the DB
            File file = new File(DBDirectory + "/PD-2022-23-TP-" + serverPort + ".db");
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

            os.close();
            fo.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public synchronized void updateDBVersion(){
        this.heartBeat.setDatabaseVersion(this.data.getDatabaseVersion());
    }

    public synchronized void closeServer() throws InterruptedException, IOException, SQLException {
        mcs.leaveGroup(sa, ni);
        mcs.close();
        this.data.closeDB();

        this.handleDB = false;
        this.dbHandler.join(1000);
        this.dbHandler.interrupt();

        heartBeat.setAvailable(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(heartBeat);
        byte[] buffer = baos.toByteArray();
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                InetAddress.getByName("239.39.39.39"), 4004);
        mcs.send(dp);

        HBHandle = false;
        hbh.join(10000);
        hbh.interrupt();

        scheduler.shutdownNow();
    }

    private synchronized void updateDB(HeartBeat hBeat) {
        this.data.processNewQuerie(hBeat.getQueries());
        this.heartBeat.resetMostRecentQuery();
    }

    public synchronized String listAllAvailableServers() {
        return this.data.listAllAvailableServers();
    }

    public synchronized void sendCommit(){
        try{
            this.heartBeat.setMessage("COMMIT");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(this.heartBeat);
            byte[] buffer = baos.toByteArray();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("239.39.39.39"), 4004);
            mcs.send(dp);

            this.heartBeat.setMessage("");
            prepare.set(false);
            masterSV.set(false);
        }catch (IOException e){
            System.out.println("sendCommit");
            sendAbort();
            return;
        }
    }
    public synchronized void sendAbort(){
        try{
            this.heartBeat.setMessage("ABORT");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(this.heartBeat);
            byte[] buffer = baos.toByteArray();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("239.39.39.39"), 4004);
            mcs.send(dp);

            this.heartBeat.setMessage("");
            prepare.set(false);
            masterSV.set(false);
        }catch (IOException e){
            return;
        }
    }
    public synchronized void sendConfirmation(String newIP, int newPort){
        InetAddress ip;
        try{
            ip = InetAddress.getByName(newIP);
        }catch (UnknownHostException e){
            return;
        }

        DatagramSocket socket = null;
        try{
            socket = new DatagramSocket();
        }catch (SocketException e){
            return;
        }

        System.out.println(newIP + " " + newPort);
        String msgConfirm = "SERVER-CONFIRMATION";
        DatagramPacket packet = new DatagramPacket(msgConfirm.getBytes(), msgConfirm.getBytes().length, ip, newPort);

        try{
            socket.send(packet);
        }catch (IOException e){
            return;
        }
    }

    public synchronized void getServersConfirmation(DatagramSocket socketUDP){
        int nTimeouts = 0;
        System.out.println("IM here");

        while(true){
            DatagramPacket packet = new DatagramPacket(new byte[256], 256);

            String messageReceived = "";
            try{
                socketUDP.receive(packet);
                messageReceived = new String(packet.getData(), 0, packet.getLength());
            }catch (SocketTimeoutException e){
                ++nTimeouts;
            }catch (IOException e){
                break;
            }

            if(messageReceived.equals("SERVER-CONFIRMATION")){
                System.out.println(messageReceived);

                ++confirmations;

                System.out.println("CONF="+confirmations + " " + "CONN="+data.getNumberOfServersConnected());

                if(confirmations >= data.getNumberOfServersConnected() - 1){
                    System.out.println("vai fazer commit");
                    sendCommit();
                    break;
                }
            }
        }
    }

    public synchronized void sendPrepare(){
        try{
            this.heartBeat.setMessage("PREPARE");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            oos.writeObject(this.heartBeat);
            byte[] buffer = baos.toByteArray();
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("239.39.39.39"), 4004);
            mcs.send(dp);
            this.heartBeat.setMessage("");
            prepare.set(true);
            masterSV.set(true);
        }catch (IOException e){
            return;
        }
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
            if(!data.connectToDB(serverPort, DBDirectory)){
                return;
            }

            while(handleDB){
                if (prepare.get()){
                    continue;
                }

                try {
                    Thread.sleep(500);
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
                        sendPrepare();
                        System.out.println("Já fez o prepare");
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
            while(HBHandle)
            {
                if (mcs.isClosed()){
                    break;
                }
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[20480], 20480);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat hBeat = (HeartBeat)ois.readObject();

                        data.processANewHeartBeat(hBeat);

                        if(hBeat.getMessage().equals("PREPARE") && hBeat.getPortTcp() != heartBeat.getPortTcp()){
                            System.out.println("PREPARE");
                            prepare .set(true);
                            sendConfirmation(hBeat.getIp(), hBeat.getPortTcp());
                        }
                        if(hBeat.getMessage().equals("ABORT") && hBeat.getPortTcp() != heartBeat.getPortTcp()){
                            System.out.println("ABORT");
                            prepare.set(false);
                        }
                        if(hBeat.getMessage().equals("COMMIT") && hBeat.getPortTcp() != heartBeat.getPortTcp()){
                            System.out.println("COMMIT");
                            hbWithHighestVersion = hBeat;
                            prepare.set(false);
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

                        if(heartBeat.getPortTcp() == serverPort){
                            continue;
                        }

                        if(dbCopyHeartBeat == null){
                            dbCopyHeartBeat = heartBeat;
                            continue;
                        }
                        if(heartBeat.getDatabaseVersion() > dbCopyHeartBeat.getDatabaseVersion()){
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

    class TCPHandler extends Thread{
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(serverPort);
            } catch (IOException e) {
                return;
            }
            Socket socket = null;
            while(true)
            {
                try {
                    socket = serverSocket.accept();
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();

                    byte[] msg = new byte[1024];
                    int nBytes = is.read(msg);
                    String msgReceived = new String(msg, 0, nBytes);

                    if(msgReceived.equals("SERVER")){
                        byte[] buffer = new byte[512];
                        int readBytes = 0;
                        FileInputStream fi = new FileInputStream(DBDirectory + "/PD-2022-23-TP-" + serverPort + ".db");

                        do
                        {
                            readBytes = fi.read(buffer);
                            if(readBytes == -1)
                                break;
                            os.write(buffer, 0, readBytes);
                        }while(readBytes > 0);

                        fi.close();
                    }

                    if(msgReceived.equals("CLIENT")){
                        System.out.println("CLIENT CONNECTED");

                        String s = prepare.get() ? "SERVER IS UPDATING - PLEASE TRY AGAIN" : "CONFIRMED";
                        os.write(s.getBytes(), 0, s.length());

                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                        DBHelper dbh = null;
                        try{
                            dbh = (DBHelper) ois.readObject();
                        }catch (ClassNotFoundException ignored){

                        }
                        System.out.println("OBJECT READ");
                        ois.close();
                        os.close();
                    }

                    socket.close();

                } catch (IOException e) {
                    break;
                }
            }

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class UDPHandler extends Thread{
        private DatagramSocket socketUDP;

        public UDPHandler(){
            try{
                socketUDP = new DatagramSocket(serverPort);
            }catch (SocketException e){
                return;
            }
        }
        @Override
        public void run() {
            while(UDPHandle){
                if(prepare.get()){
                    getServersConfirmation(socketUDP);
                    continue;
                }

                DatagramPacket packet = new DatagramPacket(new byte[256], 256);

                String messageReceived;
                try{
                    socketUDP.receive(packet);
                    messageReceived = new String(packet.getData(), 0, packet.getLength());
                }catch (IOException e){
                    continue;
                }

                if(!messageReceived.equals("CONNECTION")){
                    continue;
                }

                String msg = data.getOrderedServers();
                byte[] msgBytes = msg.getBytes();

                DatagramPacket packetToSend = new DatagramPacket(msgBytes, msgBytes.length, packet.getAddress(), packet.getPort());

                try{
                    socketUDP.send(packetToSend);
                }catch (IOException e){
                    continue;
                }
            }
        }
    }
}
