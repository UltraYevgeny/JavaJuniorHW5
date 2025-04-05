package chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void runServer() {
        try {
            while (!serverSocket.isClosed()) {
                // accept() - как только вызываем основной поток переходит в режим ожидания
                Socket socket = serverSocket.accept();
                // а, как только подключается клиент мы создаем для него отдельный поток
                ClientManager clientManager = new ClientManager(socket);
                System.out.println("Подключен новый клиент!");

                // передаем в Thread объект имплементирующий Runnable
                Thread thread = new Thread(clientManager);
                thread.start();
            }
        } catch (IOException e) {
            closeSocket();
        }
    }


    private void closeSocket() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
