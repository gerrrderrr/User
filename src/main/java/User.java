import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class User {
    public static BufferedReader reader;
    public static PrintWriter out;
    public static BufferedReader in;
    public static Socket clientSocket;
    private static final String PATH_TO_SETTINGS = "src/main/resources/settings.txt";
    private static final String PATH_TO_ID = "src/main/resources/ID.txt";
    public static final UserLogger logger = UserLogger.getInstance();


    public static void main(String[] args) {
        String host = "localHost";
        final int port = Integer.parseInt(readFromFile(PATH_TO_SETTINGS));
        try {
            logger.log("Trying to create connection port: " + port + " | host: " + host);
            clientSocket = new Socket(host, port);
            logger.log("User successfully connected to the server");
            reader = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            if (!userExist()) {
                waitForNewId();
                createNickname();
            }
            new GetMessage().start();
            logger.log("Start of GetMessage");
            new SendMessage().start();
            logger.log("Start of SendMessage");
        } catch (Exception e) {
            logger.log("Exception in User main: " + e.getMessage());
            exit();
        }
    }

    private static boolean userExist() {
        if (Files.exists(Path.of(PATH_TO_ID))) {
            String id = readFromFile(PATH_TO_ID);
            if (id != null) {
                out.println(id);
                try {
                    String answer = in.readLine();
                    if (answer.equals("User exist")) {
                        return true;
                    } else if (answer.equals("User doesn't exist")) {
                        return false;
                    }
                } catch (IOException e) {
                    logger.log("Exception in User userExist: " + e.getMessage());
                }
            }
        }
        out.println("No Id");
        return false;
    }

    private static void waitForNewId() {
        String id = null;
        try {
            id = in.readLine();
            logger.log("User received id number: " + id);
        } catch (IOException e) {
            logger.log("Exception in User waitForNewId: " + e.getMessage());
        }
        writeIdToFile(id);
    }

    private static void createNickname() {
        try {
            while (true) {
                String responseFromServer = in.readLine();
                System.out.println(responseFromServer);
                if (responseFromServer.contains("enter")) {
                    String nickname = reader.readLine();
                    out.println(nickname);
                    String result = in.readLine();
                    System.out.println(result);
                    if (result.contains("welcome")) {
                        logger.log("Nickname was successfully created: " + nickname);
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            logger.log("Exception in User createNickname: " + e.getMessage());
        }
    }

    private static boolean createNewFile() {
        try {
            return new File(PATH_TO_ID).createNewFile();
        } catch (IOException e) {
            System.out.println("Caught exception in User createNewFile: " + e.getMessage());
        }
        return false;
    }

    private static String readFromFile(String path) {
        String text = null;
        try (FileInputStream txtFromFile = new FileInputStream(path)) {
            byte[] txtInBytes = txtFromFile.readAllBytes();
            text = new String(txtInBytes);
        } catch (IOException e) {
            logger.log("Exception in User readFromFile: " + e.getMessage());
        }
        if (text == null) {
            System.out.println("Failed to read text from the file");
            logger.log("Failed to read text from the file");
        }
        return text;
    }

    private static void writeIdToFile(String id) {
        Path path = Path.of(PATH_TO_ID);
        if (Files.notExists(path)) {
            createNewFile();
            if (createNewFile()) {
                logger.log("ID.txt was successfully created");
            }
        }
        try {
            Files.write(path, id.getBytes(), StandardOpenOption.WRITE);
            logger.log("Id was written: " + id);
        } catch (IOException e) {
            logger.log("Exception in User writeIdToFile: " + e.getMessage());
        }
    }

    public static void exit() {
        if (!clientSocket.isClosed()) {
            try {
                clientSocket.close();
                logger.log("Client is closed");
                in.close();
                logger.log("BufferedReader is closed");
            } catch (IOException e) {
                logger.log("Exception in User exit: " + e.getMessage());
            }
            out.close();
            logger.log("PrintWriter is closed");
        }
    }
}
