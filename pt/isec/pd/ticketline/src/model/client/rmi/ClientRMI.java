package pt.isec.pd.ticketline.src.model.client.rmi;

import pt.isec.pd.ticketline.src.model.server.rmi.TicketLineServerRemoteInterface;
import pt.isec.pd.ticketline.src.ui.ClientRMI_UI;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/**
 *
 * Esta classe lista os servidores ativos,
 * recebe as notificacoes assincronas através de callback
 * Esta app recebe o Endereco Ip da maquina e o porto de escuta UDP
 */
public class ClientRMI extends UnicastRemoteObject implements TicketLineClientRemoteInterface {


    public static void main(String[] args) {
        ClientRMI_UI clientRMIUi = null;
        TicketLineServerRemoteInterface remoteRef;

        if (args.length != 2) {
            System.out.println("The Ip address and Port is missing from the command line arguments.");
            return;
        }

        try {
            String remoteRmi = TicketLineServerRemoteInterface.REGISTRY_BIND_NAME + "[" + Integer.parseInt(args[1]) + "]";
            //Localizar o servico de RMI com o ip da maquina e o porto de escuta UDP
            Registry r = LocateRegistry.getRegistry(args[0] , Integer.parseInt(args[1]));
            //Obter o servico remoto do servidor
            remoteRef = (TicketLineServerRemoteInterface) r.lookup(remoteRmi);

            ClientRMI clientRmi = new ClientRMI(args[0], Integer.parseInt(args[1]), remoteRef);
            clientRMIUi = new ClientRMI_UI(clientRmi);
        } catch (RemoteException | NotBoundException e) {
            System.out.println("Could not initiate ClientRmi");
        }

        assert clientRMIUi != null;
        clientRMIUi.start();
    }

    private final String ipAddress;
    private final Integer port;
    private final TicketLineServerRemoteInterface remoteRef;


    public ClientRMI(String ipAddress , Integer port, TicketLineServerRemoteInterface remoteRef) throws RemoteException, NotBoundException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.remoteRef = remoteRef;
    }

    public void list(){
        try{
            remoteRef.listActiveServers(this);
        }catch (RemoteException e) {
            System.out.println("Could not list servers");
            e.printStackTrace();
        }
    }

    public boolean addListener(String whatToListen){
        try{
            switch(whatToListen){
                case "UDP" -> remoteRef.addUDPListener(this);
                case "TCP" -> remoteRef.addTCPListener(this);
                case "LOGIN" -> remoteRef.addLoginListener(this);
                case "LOST" -> remoteRef.addLostTCPListener(this);
                default -> {
                    return false;
                }
            }
        }catch (RemoteException e){
            return false;
        }

        return true;
    }

    public boolean removeListener(String whatToRemove){
        try{
            switch (whatToRemove){
                case "UDP" -> remoteRef.removeUDPListener(this);
                case "TCP" -> remoteRef.removeTCPListener(this);
                case "LOGIN" -> remoteRef.removeLoginListener(this);
                case "LOST" -> remoteRef.removeLostTCPListener(this);
                default -> {
                    return true;
                }
            }
        }catch (RemoteException e){
            return true;
        }

        return false;
    }

    @Override
    public void listActiveServers(String list) {
        System.out.println(list);
    }

    @Override
    public void UDPListener(String ip, int port) throws RemoteException {
        System.out.println("\nNew UDP Connection: \n\tIP: " + ip + "\n\tPort: " + port + "\n\n");
    }

    @Override
    public void TCPListener(String ip, int port) throws RemoteException {
        System.out.println("\nNew TCP Connection: \n\tIP: " + ip + "\n\tPort: " + port+ "\n\n");
    }

    @Override
    public void lostTCPListener(String ip, int port, String username) throws RemoteException {
        System.out.println("\nLost a TCP Connection: \n\tIP: " + ip + "\n\tPort: " + port + "\n\tUser: " + username+ "\n\n");
    }

    @Override
    public void loginListener(String username) throws RemoteException {
        System.out.println("\nNew Login: \n\tUsername: " + username+ "\n\n");
    }
}
