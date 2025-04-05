/*
Добавить третьего клиента, и создать личные сообщения.
Формат: @имя текст сообщения
если начинается с символа @, то брать имя
если такого имени нет то сообщение отправляется всем
 */


package chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Как бы обвертка для клиента
 */
public class ClientManager implements Runnable{
    // в классе Client один способ использования Runnable
    // а, здесь мы имплементируем Runnable
    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;
    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
        this.socket = socket;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                /*
                // для macOS и Linux этот if - иначе если клиент отвалится,
                // то он будет спамить наллами.
                if (messageFromClient == null) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                */

                broadcastMessage(messageFromClient);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }


    /**
     * Отправляет сообщения другим клиентам
     * @param message
     */
    private void broadcastMessage(String message) {

            String[] nameForPrivateMassage = message.split(" ");
            if (nameForPrivateMassage[1].startsWith("@")) {
                String userName = nameForPrivateMassage[1].substring(1);

                //проверяет есть ли такое имя в списке clients
                if (clients.stream().anyMatch(client -> client.name.equals(userName)))
                    //если имя есть, то перебор forEach до нужного имени
                    //и отправка ему сообщения
                    clients.forEach(client -> {
                        if (client.name.equals(userName)) {
                            try {
                                client.bufferedWriter.write(message.replace(nameForPrivateMassage[1],
                                        "(ЛИЧНОЕ СООБЩЕНИЕ)"));
                                client.bufferedWriter.newLine();
                                client.bufferedWriter.flush();
                            } catch (IOException e) {
                                closeEverything(socket, bufferedReader, bufferedWriter);
                            }
                        }
                    });
                else {
                    try {
                        this.bufferedWriter.write("Пользователь "
                                + userName + " не найден.");
                        this.bufferedWriter.newLine();
                        this.bufferedWriter.flush();
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                    sendToAllClients(message);
                }
            } else {
                sendToAllClients(message);
            }

    }

    /**
     Транслятор сообщений всем остальным клиентам
     */
    private void sendToAllClients(String message){
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(name)) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }



    private void closeEverything(Socket socket,
                                 BufferedReader bufferedReader,
                                 BufferedWriter bufferedWriter) {
            removeClient();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }


}
