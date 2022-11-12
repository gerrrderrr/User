import java.io.IOException;

public class GetMessage extends Thread {

    @Override
    public void run() {
        while (!User.clientSocket.isClosed()) {
            String message = null;
            try {
                message = User.in.readLine();
            } catch (IOException e) {
                User.exit();
                User.logger.log("Exception in GetMessage run: " + e.getMessage());
            }
            if (message != null) {
                System.out.println(message);
            } else {
                User.exit();
                break;
            }
        }
    }
}
