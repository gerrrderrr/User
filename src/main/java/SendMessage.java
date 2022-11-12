import java.io.IOException;

public class SendMessage extends Thread {

    @Override
    public void run() {
        while (!User.clientSocket.isClosed()) {
            String message = null;
            try {
                message = User.reader.readLine();
            } catch (IOException e) {
                User.logger.log("Exception in SengMessage run: " + e.getMessage());
            }
            if (message != null && message.equalsIgnoreCase("end")) {
                User.out.println(message);
                User.exit();
                break;
            }
            User.out.println(message);
        }
    }
}
