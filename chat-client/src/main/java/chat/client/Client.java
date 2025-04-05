package chat.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


/**
 * Как бы класс обвертка для класса Socket
 */
public class Client {
    private final Socket socket; //клиентский сокет
    private final String name;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;


    public Client(Socket socket, String userName) {
        this.socket = socket;
        this.name = userName;

        try {
            // здесь socket.getOutputStream() - это поток байтов
            // и мы обворачиваем его в OutputStreamWriter
            // что бы получать данные типа текст
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    /**
     * слушатель для входящих сообщений от сервера
     */
    public void listenForMessage() {
        // новый поток (многопоточность)
        // Runnable() видимо функциональный интерфейс класса Thread
        new Thread(new Runnable() {
            // можно не переписывать метод run, а сделать через лямбда вырожение.
            @Override
            public void run() {
                String message;
                while (socket.isConnected()) {
                    try {
                        //находится в ожидании появления строки
                        message = bufferedReader.readLine();
                        System.out.println(message);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        //тут можно не закрывать, а попробовать снова
                        //соединиться с сервером
                    }
                }

            }

        }).start();
    }


    public void sendMessage() {
        try {
            bufferedWriter.write(name); //заносим в буфер свое имя
            bufferedWriter.newLine(); // переход на новую строку
            //в данном случае flush незамедлительно отправляет сообщение в поток
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                bufferedWriter.write(name + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    private void closeEverything(Socket socket,
                                 BufferedReader bufferedReader,
                                 BufferedWriter bufferedWriter) {
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

}
