package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ExecutorSendHeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ServerLifeCheck;
import pt.isec.pd.ticketline.src.ui.ServerUI;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";
    private Data data;
    private String DBDirectory;
    private volatile boolean available;
    private int numberOfConnections;
    private MulticastSocket mcs;
    private HeartBeat dbCopyHeartBeat;
    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;
    private HeartBeatReceiver hbh;
    private HeartBeat heartBeat;
    private int tcpPort;
    private ServerInit si;
    private DatabaseProvider dbProv;

    private boolean serverInitContinue;
    
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

    public Server(int port, String DBDirectory, Data data) throws SQLException, IOException, InterruptedException {
        this.data = data;
        this.available = true;
        this.numberOfConnections = 0;
        this.dbCopyHeartBeat = null;
        this.DBDirectory = DBDirectory;
        this.tcpPort = port;
        this.serverInitContinue = true;
        //START SERVER

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        mcs = new MulticastSocket(multicastPort);
        ipGroup = InetAddress.getByName(ipMulticast);
        sa = new InetSocketAddress(ipGroup, multicastPort);
        ni = NetworkInterface.getByIndex(0);
        mcs.joinGroup(sa, ni);

        heartBeat = new HeartBeat(port, available, data.getDatabaseVersion(),
                                 numberOfConnections, DBDirectory, "127.0.0.1"
                                 );
        
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

    public void transferDatabase(HeartBeat dbHeartbeat){
        if((new File(DBDirectory + "/PD-2022-23-TP-" + tcpPort + ".db")).exists()){
            System.out.println("TRANSFER-DATABASE: FILE EXISTS");
            if (dbHeartbeat == null){
                System.out.println("TRANSFER-DATABSE: GOT MY DB BUT IM FOREVER ALONE");
                return;
            }
            if(this.data.testDatabaseVersion(DBDirectory, tcpPort) >= dbHeartbeat.getDatabaseVersion()){
                System.out.println("TRANSFER-DATABASE: MY DB BIGGER");
                return;
            }
        }

        if(dbHeartbeat == null){
            System.out.println("TRANSFER-DATABASE: FIRST SERVER");
            return;
        }

        System.out.println("TRANSFER-DATABASE: GOTTA SOWNLOAD");

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

    public void closeServer() throws InterruptedException, IOException {
        heartBeat.setAvailable(false);
        hbh.join(10000);
        hbh.interrupt();
        mcs.leaveGroup(sa, ni);
        mcs.close();
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
                        HeartBeat heartBeat = (HeartBeat)ois.readObject();

                        data.processANewHeartBeat(heartBeat);
                    }
                    catch(ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }

                    data.checkForServerDeath();
                }catch (IOException e){
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
                            System.out.println("My HEARTBEAT");
                            continue;
                        }
                    
                        if(dbCopyHeartBeat == null){
                            System.out.println("FIRST HEARTBEAT RECEIVED");
                            dbCopyHeartBeat = heartBeat;
                            continue;
                        }
                        if(heartBeat.getDatabaseVersion() > dbCopyHeartBeat.getDatabaseVersion()){
                            System.out.println("FOUND BIGGER HEARTBEAT");
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
