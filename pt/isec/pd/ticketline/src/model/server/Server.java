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
    private HeartBeatReceiver hbh;
    private HeartBeat heartBeat;
    private int tcpPort;
    private ServerInit si;
    private DatabaseProvider dbProv;

    private boolean serverInitContinue;

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

        //Connect to DB
        if(!this.data.connectToDB(port, DBDirectory)){
            throw new SQLException();
        }

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
        return this.data.listUsers(userID);
    }

    public String listShows(Integer showID){
        return this.data.listShows(showID);
    }

    public String listReservations(Integer reservationID){
        return this.data.listReservations(reservationID);
    }

    public String listSeats(Integer seatID){
        return this.data.listSeats(seatID);
    }

    public void insertShow(){
        updateDBVersion();
        this.data.addShow();
    }

    public boolean insertSeat(ArrayList<ArrayList<String>> parameters , int numShow){
        boolean bool = this.data.insertSeat(parameters, numShow);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean insertReservation(ArrayList<String> parameters){
        boolean bool = this.data.insertReservation(parameters);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean insertUser(ArrayList<String> parameters){
        boolean bool = this.data.insertUser(parameters);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteShow(int id){
        boolean bool = this.data.deleteShow(id);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteReservations(int id){
        boolean bool = this.data.deleteReservations(id);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteSeat(int id){
        boolean bool = this.data.deleteSeat(id);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean deleteUsers(int id){
        boolean bool = this.data.deleteUsers(id);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }

    public boolean updateShows(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateShows(id, newData);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateSeats(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateSeats(id, newData);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateReservation(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateReservation(id, newData);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
    }
    public boolean updateUser(int id, HashMap<String, String> newData){
        boolean bool = this.data.updateUser(id, newData);

        if(bool){
            updateDBVersion();
            return true;
        }

        return false;
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
                    DatagramPacket dp = new DatagramPacket(new byte[512], 512);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat hBeat = (HeartBeat)ois.readObject();

                        data.processANewHeartBeat(hBeat);

                        if(hBeat.getDatabaseVersion() > heartBeat.getDatabaseVersion()){
                            updateDB(hBeat);
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
