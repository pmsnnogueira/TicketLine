package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.DBHelper;
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
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Server {
    public static void main(String[] args){
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

    private final Data data;
    private final String DBDirectory;
    private volatile boolean available;
    private int numberOfConnections;
    private HeartBeat dbCopyHeartBeat;
    private HeartBeat heartBeat;
    private HeartBeat hbWithHighestVersion;
    private HeartBeatReceiver hbh;
    private DataBaseHandler dbHandler;
    private DBHelper dbHelper = null;

    private LinkedList<DBHelper> listDbHelper;
    private int serverPort;
    private ServerInit si;
    private TCPHandler tcpHandler;

    private AtomicReference<Boolean> serverInitContinue;
    private AtomicReference<Boolean> handleDB;

    private MulticastSocket mcs;

    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;
    private ScheduledExecutorService scheduler;
    private AtomicReference<Boolean> HBHandle;
    private UDPHandler udpHandler;
    private AtomicReference<Boolean> UDPHandle;
    private AtomicReference<Boolean> prepare;
    private AtomicReference<Boolean> masterSV;
    private AtomicReference<Integer> proceed;
    private ScheduledFuture<?> sendHeartBeatExecutor;
    private ScheduledFuture<?> serverLifeCheckExecutor;
    private AtomicReference<Boolean> tcpHandle;
    private int confirmations;

    private ArrayList<AtomicReference<Boolean>> listClientHandles;
    private ArrayList<ClientHandler> clients;

    public Server(int port, String DBDirectory) throws SQLException, IOException, InterruptedException {
        this.prepare = new AtomicReference<>(false);
        this.masterSV = new AtomicReference<>(false);

        this.available = true;
        this.numberOfConnections = 0;
        this.dbCopyHeartBeat = null;
        this.DBDirectory = DBDirectory;
        this.serverPort = port;
        this.serverInitContinue = new AtomicReference<>(true);
        this.mcs = new MulticastSocket(Integer.parseInt(MULTICAST.getValue(1)));
        this.ipGroup = InetAddress.getByName( MULTICAST.getValue(0));
        this.sa = new InetSocketAddress(ipGroup, Integer.parseInt( MULTICAST.getValue(1)));
        this.ni = NetworkInterface.getByIndex(0);
        this.mcs.joinGroup(sa, ni);

        this.data = new Data();
        //START SERVER

        this.scheduler = Executors.newScheduledThreadPool(2);

        // server initiaton phase
        si = new ServerInit();
        si.start();
        Thread.sleep(10000);
        serverInitContinue.set(false);
        si.join(1000);
        si.interrupt();

        transferDatabase(dbCopyHeartBeat);

        //start thread to handle the DB operations
        this.hbWithHighestVersion = null;
        this.handleDB = new AtomicReference<>(true);
        dbHelper = null;
        listDbHelper = new LinkedList<>();

        dbHandler = new DataBaseHandler();
        this.dbHandler.start();


        heartBeat = new HeartBeat(port, available, this.data.getDatabaseVersion(),
                numberOfConnections, DBDirectory, "127.0.0.1");

        this.data.setServerHeartBeat(heartBeat);

        //Every 10 seconds, the server will send a heart beat through multicast
        //to every other on-line server
        sendHeartBeatExecutor = scheduler.scheduleAtFixedRate(new ExecutorSendHeartBeat(heartBeat, mcs),
                0, 10, TimeUnit.SECONDS);
        //Every 35 seconds, the server will check if there is any server
        //who hasn't sent a heartbeat in the last 35 secs
        serverLifeCheckExecutor = scheduler.scheduleAtFixedRate(new ServerLifeCheck(this.data), 0, 35, TimeUnit.SECONDS);

        //start thread to receive the heartbeats
        this.HBHandle = new AtomicReference<>(true);
        hbh = new HeartBeatReceiver();
        hbh.start();

        //start thread to handle tcp connections
        this.tcpHandle = new AtomicReference<>(true);
        tcpHandler = new TCPHandler();
        tcpHandler.start();

        //start thread to handle udp connections
        this.UDPHandle = new AtomicReference<>(true);
        this.udpHandler = new UDPHandler();
        this.udpHandler.start();

        //lists for clients
        this.listClientHandles = new ArrayList<>();
        this.clients = new ArrayList<>();
    }

    public String listUsers(Integer userID){
        dbHelper = new DBHelper();
        dbHelper.setId(userID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("user");
        listDbHelper.add(dbHelper);
        return "";
    }

    public String listShows(Integer showID){
        this.dbHelper.setId(showID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("show");

        return "";
    }

    public String listReservations(Integer reservationID){
        this.dbHelper.setId(reservationID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("reservation");

        return "";
    }

    public String listSeats(Integer seatID){
        this.dbHelper.setId(seatID);
        this.dbHelper.setOperation("SELECT");
        this.dbHelper.setTable("seat");

        return "";
    }

    public void insertShow(){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("show");

    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("seat");
        this.dbHelper.setSeatParams(parameters);
        this.dbHelper.setId(numShow);

        return true;
    }

    public boolean insertReservation(ArrayList<String> parameters){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setInsertParams(parameters);

        return true;
    }

    public boolean insertUser(ArrayList<String> parameters){
        this.dbHelper.setOperation("INSERT");
        this.dbHelper.setTable("user");
        this.dbHelper.setInsertParams(parameters);

        return true;
    }

    public boolean deleteShow(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("show");
        this.dbHelper.setId(id);

        return true;
    }

    public boolean deleteReservations(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setId(id);

        return true;
    }

    public boolean deleteSeat(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("seat");
        this.dbHelper.setId(id);

        return true;
    }

    public boolean deleteUsers(int id){
        this.dbHelper.setOperation("DELETE");
        this.dbHelper.setTable("user");
        this.dbHelper.setId(id);

        return true;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("show");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);

        return true;
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("seat");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);

        return true;
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("reservation");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
        return true;
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        this.dbHelper.setOperation("UPDATE");
        this.dbHelper.setTable("user");
        this.dbHelper.setId(id);
        this.dbHelper.setUpdateParams(newData);
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
        heartBeat.setAvailable(false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(heartBeat);
        byte[] buffer = baos.toByteArray();
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
                InetAddress.getByName(MULTICAST.getValue(0)),Integer.parseInt(MULTICAST.getValue(1)));
        mcs.send(dp);

        for(AtomicReference<Boolean> b : listClientHandles){
            b.set(false);
        }

        for(ClientHandler ch : clients){
            ch.join(3000);
            ch.interrupt();
        }

        this.handleDB.set(false);
        this.dbHandler.join(5000);
        this.dbHandler.interrupt();

        this.data.closeDB();

        tcpHandle.set(false);
        tcpHandler.join(5000);
        tcpHandler.interrupt();

        UDPHandle.set(false);
        udpHandler.join(5000);
        udpHandler.interrupt();

        HBHandle.set(false);
        hbh.join(5000);
        hbh.interrupt();

        sendHeartBeatExecutor.cancel(true);
        serverLifeCheckExecutor.cancel(true);

        mcs.leaveGroup(sa, ni);
        mcs.close();
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
                    InetAddress.getByName( MULTICAST.getValue(0)), Integer.parseInt( MULTICAST.getValue(1)));
            mcs.send(dp);

            this.heartBeat.setMessage("");
            prepare.set(false);
            masterSV.set(false);

            listDbHelper.pop();
        }catch (IOException e){
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
                    InetAddress.getByName( MULTICAST.getValue(0)), Integer.parseInt( MULTICAST.getValue(1)));
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

        String msgConfirm = "SERVER-CONFIRMATION";
        DatagramPacket packet = new DatagramPacket(msgConfirm.getBytes(), msgConfirm.getBytes().length, ip, newPort);

        try{
            socket.send(packet);
        }catch (IOException e){
            socket.close();
            return;
        }
        socket.close();
    }

    public synchronized void getServersConfirmation(DatagramSocket socketUDP){
        int nTimeouts = 0;

        while(true){
            if(nTimeouts >= 10){
                sendAbort();
                break;
            }

            DatagramPacket packet = new DatagramPacket(new byte[256], 256);

            String messageReceived = "";
            try{
                socketUDP.receive(packet);
                messageReceived = new String(packet.getData(), 0, packet.getLength());
            }catch (SocketTimeoutException e){
                ++nTimeouts;
            }catch (IOException e){
                sendAbort();
                break;
            }

            if(messageReceived.equals("SERVER-CONFIRMATION")){
                ++confirmations;

                if(confirmations >= data.getNumberOfServersConnected() - 1){
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
                    InetAddress.getByName( MULTICAST.getValue(0)), Integer.parseInt( MULTICAST.getValue(1)));
            mcs.send(dp);
            this.heartBeat.setMessage("");
            prepare.set(true);
            masterSV.set(true);
        }catch (IOException e){
            return;
        }
    }

    class DataBaseHandler extends Thread{
        @Override
        public void run() {
            //Connect to DB
            if(!data.connectToDB(serverPort, DBDirectory)){
                return;
            }

            while(handleDB.get()){
                //If it is during the synchronization phase
                //this thread will not do anything
                if (prepare.get()){
                    continue;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //if it has received the command to COMMIT changes to DB
                //gets the heartbeat that sent said command and gets its
                //most recent query to update de DB
                if(hbWithHighestVersion != null){
                    updateDB(hbWithHighestVersion);
                    hbWithHighestVersion = null;
                    updateDBVersion();
                }

                //if it has pending request
                if (listDbHelper.size() > 0 ){
                    String requestResult = null;
                    DBHelper dbHelper = listDbHelper.get(0);

                    //if the request has already been processed
                    //it means that the synchronization must have gone wrong,
                    //so it will start the process again
                    if(!dbHelper.isAlreadyProcessed()){
                        switch (dbHelper.getOperation()){
                            case "INSERT"->{
                                switch (dbHelper.getTable()){
                                    case "show" ->{
                                        data.addShow();
                                        requestResult = String.valueOf("Show Inserted");
                                    }
                                    case "seat" ->{
                                        requestResult = String.valueOf(data.insertSeat(dbHelper.getSeatParams(), dbHelper.getId()));
                                    }
                                    case "reservation" ->{
                                        requestResult = String.valueOf(data.insertReservation(dbHelper.getInsertParams()));
                                    }
                                    case "reservation_seat" ->{
                                        requestResult = String.valueOf(data.insertReservationSeat(dbHelper.getInsertParams() , dbHelper.getId()));
                                    }
                                    case "user" ->{
                                        requestResult = String.valueOf(data.insertUser(dbHelper.getInsertParams()));
                                    }
                                }
                            }
                            case "SELECT"->{
                                switch (dbHelper.getTable()){
                                    case "show" ->{
                                        if(dbHelper.getOption() != null){
                                            switch (dbHelper.getOption()){
                                                case 2 ->{
                                                     requestResult = data.listEmptySeatsDayBefore(dbHelper.getId());
                                                }
                                            }
                                        }else
                                            requestResult = data.listShows(dbHelper.getId());
                                    }
                                    case "seat" ->{
                                        requestResult = data.listSeats(dbHelper.getId());
                                    }
                                    case "reservation" ->{
                                        if(dbHelper.getOption() != null){
                                           if(dbHelper.getOption() == 3 || dbHelper.getOption() == 4)
                                                    requestResult = data.listNotOrPaidReservations(dbHelper.getId(),dbHelper.getInsertParams());
                                        }else
                                            requestResult = data.listReservations(dbHelper.getId());
                                    }
                                    case "user" ->{
                                        if(dbHelper.getverifyUsername() != null){
                                            requestResult = data.verifyUserLogin(dbHelper.getverifyUsername());
                                        }
                                        else {
                                            requestResult = data.listUsers(dbHelper.getId());
                                        }
                                    }
                                }
                            }
                            case "UPDATE"->{
                                switch (dbHelper.getTable()){
                                    case "show" ->{
                                        requestResult = String.valueOf(data.updateShows(dbHelper.getId(),dbHelper.getUpdateParams()));
                                    }
                                    case "seat" ->{
                                        requestResult = String.valueOf(data.updateSeats(dbHelper.getId(), dbHelper.getUpdateParams()));
                                    }
                                    case "reservation" ->{
                                        requestResult = String.valueOf(data.updateReservation(dbHelper.getId(), dbHelper.getUpdateParams()));
                                    }
                                    case "user" ->{
                                        requestResult = String.valueOf(data.updateUser(dbHelper.getId(), dbHelper.getUpdateParams()));
                                    }
                                }
                            }
                            case "DELETE"->{
                                switch (dbHelper.getTable()){
                                    case "show" ->{
                                        requestResult = String.valueOf(data.deleteShow(dbHelper.getId()));
                                    }
                                    case "seat" ->{
                                        requestResult = String.valueOf(data.deleteSeat(dbHelper.getId()));
                                    }
                                    case "reservation" ->{
                                        if(dbHelper.getOption() != null)
                                            if(dbHelper.getOption() == 1) {
                                                requestResult = data.listNotOrPaidReservations(dbHelper.getId(), dbHelper.getInsertParams());    // IdReserva , IdUser
                                                requestResult = String.valueOf(data.deleteReservations(dbHelper.getId()));
                                            }
                                    }
                                    case "user" ->{
                                        requestResult = String.valueOf(data.deleteUsers(dbHelper.getId()));
                                    }
                                }
                            }
                        }
                        dbHelper.setRequestResult(requestResult);
                        dbHelper.setAlreadyProcessed(true);

                        //if the operation did not alter the DB
                        //there's no need to update the version nor
                        //to star the synchronization process
                        if(!dbHelper.getOperation().equals("SELECT")){
                            updateDBVersion();
                            sendPrepare();
                        }

                        //if the operation was a mere select
                        //we can automatically remove it from the list
                        if(dbHelper.getOperation().equals("SELECT")){
                            listDbHelper.pop();
                        }
                    }else {
                        sendPrepare();
                    }
                }
            }
        }
    }

    class HeartBeatReceiver extends Thread{
        @Override
        public void run() {
            while(HBHandle.get())
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
                            prepare .set(true);
                            try{
                                Thread.sleep(800);
                            }catch (InterruptedException ignored){}
                            sendConfirmation(hBeat.getIp(), hBeat.getPortTcp());
                        }
                        if(hBeat.getMessage().equals("ABORT") && hBeat.getPortTcp() != heartBeat.getPortTcp()){
                            prepare.set(false);
                        }
                        if(hBeat.getMessage().equals("COMMIT") && hBeat.getPortTcp() != heartBeat.getPortTcp()){
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
            while(serverInitContinue.get())
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
            while(tcpHandle.get())
            {
                try {
                    socket = serverSocket.accept();
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();

                    byte[] msg = new byte[1024];
                    int nBytes = is.read(msg);
                    String msgReceived = new String(msg, 0, nBytes);

                    if(msgReceived.equals("SERVER")){ // when server communicates with another server
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
                        socket.close();
                        continue;
                    }

                    if(msgReceived.equals("CLIENT")){// when the server receives a new request from a client
                        //start a new thread to take care of the new client
                        AtomicReference<Boolean> nHandle = new AtomicReference<>(true);
                        ClientHandler c = new ClientHandler(socket, nHandle, os, is);
                        c.start();

                        clients.add(c);
                        listClientHandles.add(nHandle);

                        heartBeat.setNumberOfConnections(clients.size());
                    }

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
                socketUDP.setSoTimeout(1000);
            }catch (SocketException e){
                return;
            }
        }
        @Override
        public void run() {
            while(UDPHandle.get()){
                if(prepare.get() && masterSV.get()){
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

    class ClientHandler extends Thread{
        private Socket clientSocket;
        private AtomicReference<Boolean> handle;
        private OutputStream os;
        private InputStream is;
        private ObjectOutputStream oos;
        private ObjectInputStream ois;
        private DBHelper dbHelper;

        public ClientHandler(Socket clientSocket, AtomicReference<Boolean> handle,
                             OutputStream os, InputStream is){
            this.clientSocket = clientSocket;
            this.handle = handle;
            this.os = os;
            this.is = is;
            this.oos = null;
            this.ois = null;
            this.dbHelper = new DBHelper();
        }

        @Override
        public void run() {
            while (handle.get()){
                try {
                    byte[] msg = new byte[1024];
                    int nBytes = is.read(msg);
                    String msgReceived = new String(msg, 0, nBytes);

                    if(!msgReceived.equals("NEW REQUEST")){
                        continue;
                    }

                    String s = prepare.get() ? "SERVER IS UPDATING - PLEASE TRY AGAIN" : "CONFIRMED";
                    os.write(s.getBytes(), 0, s.length());

                    if(prepare.get()){
                        clientSocket.close();
                    }

                    if(oos == null){
                        oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    }

                    oos.writeObject(data.getOrderedServers());

                    if(ois == null){
                        ois = new ObjectInputStream(clientSocket.getInputStream());
                    }

                    this.dbHelper = null;
                    try{
                        this.dbHelper = (DBHelper) ois.readObject();
                        listDbHelper.add(this.dbHelper);
                    }catch (ClassNotFoundException  e){
                        e.printStackTrace();
                    }

                    while(true){
                        if(!this.dbHelper.getRequestResult().equals("")){
                            oos.writeObject(this.dbHelper.getRequestResult());
                            this.dbHelper.setRequestResult("");
                            break;
                        }
                    }

                }catch (IOException e){
                    clients.remove(this);
                    listClientHandles.remove(this.handle);
                    heartBeat.setNumberOfConnections(clients.size());
                    try {
                        oos.close();
                        ois.close();
                        os.close();
                        is.close();
                        clientSocket.close();
                    } catch (IOException ex) {
                        return;
                    }

                    return;
                }
            }
        }
    }
}
