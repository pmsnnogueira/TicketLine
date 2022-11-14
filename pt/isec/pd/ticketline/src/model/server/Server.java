package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ExecutorSendHeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ServerLifeCheck;
import pt.isec.pd.ticketline.src.ui.ServerUI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";
    private Data data;
    private volatile boolean available;
    private int databaseVersion;
    private int numberOfConnections;
    private MulticastSocket mcs;
    private HeartBeat dbCopyHeartBeat;
    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;

    private HeartBeatReceiver hbh;
    private HeartBeat heartBeat;
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
        this.databaseVersion = 1;
        this.numberOfConnections = 0;
        this.dbCopyHeartBeat = null;
        //START SERVER

        //Connect to DB
        if(!this.data.connectToDB(port, DBDirectory)){
            throw new SQLException();
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        mcs = new MulticastSocket(multicastPort);
        ipGroup = InetAddress.getByName(ipMulticast);
        sa = new InetSocketAddress(ipGroup, multicastPort);
        ni = NetworkInterface.getByIndex(0);
        mcs.joinGroup(sa, ni);

        heartBeat = new HeartBeat(port, available, databaseVersion, numberOfConnections, DBDirectory);

        //Every 10 seconds, the server will send a heart beat through multicast
        //to every other on-line server
        scheduler.scheduleAtFixedRate(new ExecutorSendHeartBeat(heartBeat, mcs),
                0, 10, TimeUnit.SECONDS);
        //Every 35 seconds, the server will check if there is any server who hasn't
        scheduler.scheduleAtFixedRate(new ServerLifeCheck(this.data), 0, 35, TimeUnit.SECONDS);

        //start thread to receive the heartbeats
        hbh = new HeartBeatReceiver();
        hbh.start();
    }

    public void closeServer() throws InterruptedException, IOException {
        heartBeat.setAvailable(false);
        hbh.join(10000);
        mcs.leaveGroup(sa, ni);
        mcs.close();
    }

    class HeartBeatReceiver extends Thread{
        @Override
        public void run() {
            while(true)
            {
                if (mcs.isClosed()){
                    break;
                }
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
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
            while(true)
            {
                if (mcs.isClosed()){
                    break;
                }
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        heartBeat = (HeartBeat)ois.readObject();
                        if(dbCopyHeartBeat == null){
                            dbCopyHeartBeat = heartBeat;
                            continue;
                        }
                        if(heartBeat.getDatabaseVersion() > dbCopyHeartBeat.getDatabaseVersion())
                            dbCopyHeartBeat = heartBeat;
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

    
}
