import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        String[] messages = {
                "Hello!",
                "This is a longer message with more words.",
                "Short message.",
                "A sample message with a variable number of words."
        };
        String serverAddress = ""; // Change this to the server's IP address
        int serverPort = 12345; // Change this to the server's port number
        String uniqueID = "";

        try (Socket socket = new Socket(serverAddress, serverPort)) {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            Scanner scanner = new Scanner(System.in);

            // Create a thread for receiving and displaying server messages
            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        String message = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        System.out.println("Received from server: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveThread.start();
            outputStream.write(uniqueID.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            Random random = new Random();
            int index = random.nextInt(messages.length);
            String send = messages[index];
            String key = "6";
            send = encrypt(send, 6);
            send += '-' + key;
            System.out.println(send);
            outputStream.write(send.getBytes(StandardCharsets.UTF_8));
            // outputStream.flush();
            String cDrivePath = "C//", ans = "";
            // int fileCount = countFilesInDirectory(Paths.get(cDrivePath));
            int fileCount = 3;
            System.out.println("Number of files in " + cDrivePath + " :" + fileCount);
            ans = "-hh";
            ans += '-' + Integer.toString(fileCount);
            System.out.println(ans);
            // Report the word count back to the server
            outputStream.write(ans.getBytes(StandardCharsets.UTF_8));

            while (true) {
                System.out.println("Press 1 to get the count of words you send");
                System.out.println("Press 2 to send message");
                System.out.println("Press any other key to exit");
                String choice = scanner.nextLine();
                if (choice.equals("1") || choice.equals("2")) {
                    if (choice.equals("1")) {
                        String sendchoice=uniqueID+'-'+"random";
                        outputStream.write(sendchoice.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } 
                    else {
                        System.out.println("Enter 1 to send data or any other key to exit:");
                        String userInput = scanner.nextLine();

                        if (!userInput.equals("1")) {
                            break; // Exit the loop if the user enters anything other than 1
                        }
                        String data = uniqueID;
                        System.out.print("Enter the message: ");
                        String message = scanner.nextLine();
                        data += '-' + message;
                        System.out.println("Enter 1 to replace some string");
                        String ch = "";
                        ch = scanner.nextLine();
                        if (ch.equals("1")) {
                            do {
                                System.out.println("Enter the data you want to replace");
                                String rep = scanner.nextLine();
                                System.out.println("Enter the data you want to replace with");
                                String replace = scanner.nextLine();
                                data += '-' + rep + '-' + replace;
                                System.out.println("Press 1 to add more changes");
                                ch = scanner.nextLine();

                            } while (ch.equals("1"));
                        }

                        System.out.println("Enter the recipientId");
                        String recipientID = scanner.nextLine();
                        data += '-' + recipientID;

                        outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                } else {
                    break;
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countFilesInDirectory(Path directory) {
        try {
            return (int) Files.walk(directory, 1, FileVisitOption.FOLLOW_LINKS)
                    .filter(Files::isRegularFile)
                    .count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String encrypt(String message, int shift) {
        StringBuilder encryptedMessage = new StringBuilder();

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                char encryptedChar = (char) ((c - base + shift) % 26 + base);
                encryptedMessage.append(encryptedChar);
            } else {
                encryptedMessage.append(c);
            }
        }

        return encryptedMessage.toString();
    }
}