import java.io.*;
import java.net.*;
import java.util.*;

public class Server extends CensoredClass {

    public static Map<String, Socket> clientData = new HashMap<>();

    public static void main(String[] args) {
        int port = 12345; // Change this to your desired port number
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            InetAddress ip;
            ip = InetAddress.getLocalHost();
            System.out.println(ip);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Create a new thread to handle the client
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String data = new String(buffer, 0, bytesRead, "UTF-8");
                    System.out.println("Received data from " + clientAddress + ": " + data);

                    // Parse the received data into unique ID, message, and number
                    String[] parts = data.split("-");
                    System.out.println(parts.length);
                    if (parts.length == 1) {
                        clientData.put(parts[0], clientSocket);
                    } else if (parts.length == 4) {

                        System.out.println("Recieved message:" + parts[0]);
                        String[] a = parts[0].split(" ");
                        System.out.println("No of word encountered in the received message is:" + a.length);
                        System.out.println("No of file reported in C drive is: " + parts[3]);

                    } else {
                        if (parts.length == 2) {
                            String uniqueID = parts[0];
                            int count = countWords(uniqueID);
                            String result = "No of words on your file is : " + Integer.toString(count);
                            outputStream.write(result.getBytes("UTF-8"));
                            outputStream.flush();
                        } else {
                            String uniqueID = parts[0];
                            String message = parts[1];
                            int number = Integer.parseInt(parts[parts.length - 1]);
                            System.out.println("Received unique ID: " + uniqueID);
                            System.out.println("Received message: " + message);
                            System.out.println("Received number: " + number);
                            String[] arr = parts[1].split(" ");
                            if (parts.length > 3) {

                                for (int i = 2; i < parts.length - 1; i++) {
                                    replace(arr, parts[i], parts[i + 1]);
                                    i++;
                                }
                            }
                            CensoredClass obj = new CensoredClass();
                            censored(arr, obj);
                            message = arrayToStringWithSpace(arr);
                            File file = new File(uniqueID + ".txt");
                            String fileName = uniqueID + ".txt";

                            System.out.println("Message send:" + message);
                            String result = "Message send successfully!";
                            // Check if a message needs to be forwarded to another client
                            if (number > 0) {
                                String recipientID = Integer.toString(number);
                                // String recipientAddress = clientData.get(recipientID);

                                Socket recipientSocket = clientData.get(recipientID);
                                if (recipientSocket != null) {

                                    OutputStream output = recipientSocket.getOutputStream();
                                    output.write(("Message from Server: " + message).getBytes());
                                    output.flush();
                                } else {
                                    result = "User does not Exist!";
                                    System.out.println("Invalid id");
                                }
                                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                                    // Write content to the file
                                    writer.write(message);
                                    writer.write(" ");
                                    System.out.println("Content written to " + fileName);

                                } catch (IOException e) {
                                    // Handle the exception, e.g., file not found or unable to write
                                    e.printStackTrace();
                                }
                            }

                            // Send a response back to the client
                            outputStream.write(result.getBytes("UTF-8"));
                            outputStream.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Connection with " + clientSocket.getInetAddress() + " closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void sendMessageToClient(String recipientAddress, String message) {
        try (Socket recipientSocket = new Socket(recipientAddress, 12345)) {
            OutputStream outputStream = recipientSocket.getOutputStream();
            outputStream.write(message.getBytes("UTF-8"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void replace(String[] arr, String received, String replacement) {
        // System.out.println(received+" "+replacement);
        for (int i = 0; i < arr.length; i++) {
            // System.out.println(received+" "+arr[i]);
            if (arr[i].equals(received)) {

                arr[i] = replacement;
            }
        }
    }

    private static void censored(String[] arr, CensoredClass obj) {
        for (int i = 0; i < arr.length; i++) {
            if (obj.censorshipMap.containsKey(arr[i])) {
                arr[i] = obj.censorshipMap.get(arr[i]);
            }
        }
    }

    public static String arrayToStringWithSpace(String[] array) {
        StringBuilder resultBuilder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            resultBuilder.append(array[i]);
            if (i < array.length - 1) {
                resultBuilder.append(" ");
            }
        }

        return resultBuilder.toString();
    }

    private static int countWords(String fileName) {
        fileName = fileName + ".txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            int count = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+"); // Split by whitespace
                count += words.length;
            }
            return count;

        } catch (IOException e) {
            // Handle the exception, e.g., file not found
            return -1;
        }
    }
}