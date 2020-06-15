import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static final int serverPort = 1111;
    private static final String localhost = "127.0.0.1";

    public static void main(String[] args) {
        Socket socket = null;
        try {
            try {
                System.out.println("Welcome to Client side\n" +
                        "Connecting to the server\n\t" +
                        "(IP address " + localhost +
                        ", port " + serverPort + ")");
                InetAddress ipAddress = InetAddress.getByName(localhost);
                socket = new Socket(ipAddress, serverPort);
                System.out.println(
                        "The connection is established.");
                System.out.println(
                        "\tLocalPort = " +
                                socket.getLocalPort() +
                                "\n\tInetAddress.HostAddress = " +
                                socket.getInetAddress()
                                        .getHostAddress() +
                                "\n\tReceiveBufferSize (SO_RCVBUF) = "
                                + socket.getReceiveBufferSize());

                //меню
                while (!socket.isOutputShutdown()) {
                    System.out.println("You are on the menu. Enter “1” to upload the file to the system " +
                            "or “2” to receive the file from the server\n" +
                            "or \"3\" to see all server file names ");
                    Scanner console = new Scanner(System.in);
                    String commandMenu = console.nextLine();
                    DataOutputStream outMenu = new DataOutputStream(socket.getOutputStream());
                    outMenu.writeUTF(commandMenu);
                    while (commandMenu.equals("1") || commandMenu.equals("2") || commandMenu.equals("3")) {
                        if (commandMenu.equals("1")) {
                            ExchangeWithServer.upload(socket);
                            break;
                        } if (commandMenu.equals("2")) {
                            ExchangeWithServer.getFile(socket);
                            break;
                        }
                        else {
                            ExchangeWithServer.allFiles(socket);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (socket != null)
                    socket.close();
                System.err.println("Socket closed ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


