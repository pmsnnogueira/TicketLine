package pt.isec.pd.ticketline.src.model.server;

import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int multicastPort = 4004;
    private static final String ipMulticast = "239.39.39.39";

    public static void main(String[] args) throws IOException, InterruptedException {
        int portTcp = Integer.parseInt(args[0]);
        Server server = new Server();
        server.startServer(portTcp);
    }

    public void startServer(int port) throws IOException, InterruptedException, NumberFormatException{
        boolean available = true;
        int databaseVersion = 1;
        int numberOfConnections = 0;

        //String databaseDirectory = args[1];
        //ThreadTcpConnection serverTcp = new ThreadTcpConnection(portTcp, databaseDirectory);
        //serverTcp.start();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        MulticastSocket mcs = new MulticastSocket(multicastPort);
        InetAddress ipGroup = InetAddress.getByName(ipMulticast);
        SocketAddress sa = new InetSocketAddress(ipGroup, multicastPort);
        NetworkInterface ni = NetworkInterface.getByIndex(0);
        mcs.joinGroup(sa, ni);

        HeartBeat heartBeat = new HeartBeat(port, available, databaseVersion, numberOfConnections);

        scheduler.scheduleAtFixedRate(new ExecuterSendHeartBeat(heartBeat, mcs),
                        0, 10, TimeUnit.SECONDS);

        HeartBeatReceiver hbh = new HeartBeatReceiver(mcs);
        hbh.start();

        while(true){
            int input = InputProtection.chooseOption("SERVER: ", "Exit");
            if (input == 1) {
                heartBeat.setAvailable(false);
                System.out.println("GoodBye");
                break;
            }
            System.out.println("Not a valid option!");
        }

        hbh.join(10000);
        scheduler.close();
        mcs.leaveGroup(sa, ni);
        mcs.close();

    }

    class HeartBeatReceiver extends Thread{
        private MulticastSocket mcs;
        ArrayList<HeartBeat> svHB;

        public HeartBeatReceiver(MulticastSocket mcs){
            this.mcs = mcs;
            this.svHB = new ArrayList<>();
        }

        @Override
        public void run() {
            while(true)
            {
                try{
                    DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                    mcs.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    try
                    {
                        HeartBeat heartBeat = (HeartBeat)ois.readObject();
                        System.out.println("\nReceived heartbeat from server -> Port:[" + heartBeat.getPortTcp() +
                                "] Available -> [" + heartBeat.getAvailable() + "] Database version -> [" + heartBeat.getdatabaseVersion()
                                + "] Number of connections -> [" + heartBeat.getnumberOfConnections() + "]\n");

                        //if we already had a heartbeat from the same port
                        //we will replace the old one with the new one
                        svHB.removeIf(beat -> beat.getPortTcp() == heartBeat.getPortTcp());

                        svHB.add(heartBeat);
                    }
                    catch(ClassNotFoundException cnfe){
                        cnfe.printStackTrace();
                    }

                    for (HeartBeat beat : svHB){
                        System.out.println(beat.hashCode() + " " + beat.getAvailable());
                    }

                    //if there is any hearbeat not available
                    svHB.removeIf(hb -> !hb.getAvailable());
                }catch (IOException e){
                    System.out.println("Port isnt available");
                    break;
                }
            }
        }
    }
}
