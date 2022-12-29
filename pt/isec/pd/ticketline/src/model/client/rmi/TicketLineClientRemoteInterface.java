package pt.isec.pd.ticketline.src.model.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TicketLineClientRemoteInterface extends Remote {
    void listActiveServers(String list) throws RemoteException;
    void lostTCPListener(String ip, int port, String username) throws RemoteException;
    void loginListener(String username) throws RemoteException;
    void UDPListener(String ip, int port) throws RemoteException;
    void TCPListener(String ip, int port) throws RemoteException;
    void logoutListener(String username) throws RemoteException;
}
