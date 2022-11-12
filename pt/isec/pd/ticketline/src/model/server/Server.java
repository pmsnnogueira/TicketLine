package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ExecutorSendHeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.model.server.heartbeat.ServerLifeCheck;
import pt.isec.pd.ticketline.src.ui.UI;

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
    private UI ui;

    public static void main(String[] args)
    {
        try{
            Server server = new Server(Integer.parseInt(args[0]));
        }catch (SQLException | IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public Server(int port) throws SQLException, IOException, InterruptedException {
        this.ui = new UI(new ModelManager(port));
        startServer(port);
    }

    public void startServer(int port) throws IOException, InterruptedException, NumberFormatException, SQLException {
        boolean available = true;
        int databaseVersion = 1;
        int numberOfConnections = 0;

        //String databaseDirectory = args[1];
        //ThreadTcpConnection serverTcp = new ThreadTcpConnection(portTcp, databaseDirectory);
        //serverTcp.start();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        MulticastSocket mcs = new MulticastSocket(multicastPort);
        InetAddress ipGroup = InetAddress.getByName(ipMulticast);
        SocketAddress sa = new InetSocketAddress(ipGroup, multicastPort);
        NetworkInterface ni = NetworkInterface.getByIndex(0);
        mcs.joinGroup(sa, ni);

        HeartBeat heartBeat = new HeartBeat(port, available, databaseVersion, numberOfConnections);

        //Every 10 seconds, the server will send a heart beat through multicast
        //to every other on-line server
        scheduler.scheduleAtFixedRate(new ExecutorSendHeartBeat(heartBeat, mcs),
                        0, 10, TimeUnit.SECONDS);
        //Every 35 seconds, the server will check if there is any server who hasn't
        scheduler.scheduleAtFixedRate(new ServerLifeCheck(this.ui), 0, 35, TimeUnit.SECONDS);

        HeartBeatReceiver hbh = new HeartBeatReceiver(mcs);
        hbh.start();

        ui.start();
        heartBeat.setAvailable(false);

        hbh.join(10000);
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
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat heartBeat = (HeartBeat)ois.readObject();

/*                        System.out.println("\nReceived heartbeat from server -> Port:[" + heartBeat.getPortTcp() +
                                "] Available -> [" + heartBeat.getAvailable() + "] Database version -> [" + heartBeat.getdatabaseVersion()
                                + "] Number of connections -> [" + heartBeat.getnumberOfConnections() + "]\n");*/

                        ui.processANewHeartBeat(heartBeat);

                    }
                    catch(ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }

                    ui.checkForServerDeath();
                }catch (IOException e){
                    break;
                }
            }
        }
    }
}
