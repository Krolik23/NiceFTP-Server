import java.net.ServerSocket;
import java.net.Socket;

public class ServerFTP {
    public static void main(String arg[]) throws Exception
    {
        ServerSocket serverSocket = new ServerSocket(5589);
        System.out.println("FTP server works on port 5589");
        System.out.println("Connecting...");
        while(true){

            FileTransferProtocolClass transferClass = new FileTransferProtocolClass(serverSocket.accept());
                //socketCounter++;




        }

    }

}
