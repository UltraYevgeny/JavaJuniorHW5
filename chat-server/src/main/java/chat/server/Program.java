package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Program {
    public static void main(String[] args) {

        try {
            // ServerSocket - сам назначит свободный порт для каждого клиента
            // после того как тот вначале подключится к порту 1400
            ServerSocket serverSocket = new ServerSocket(1400);
            Server server = new Server(serverSocket);
            server.runServer();
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
}
