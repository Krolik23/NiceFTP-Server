import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileTransferProtocolClass extends Thread{

    Socket clientCommunicationSocket;
    DataInputStream clientCommunicationDataInput;
    DataOutputStream clientCommunicationDataOutput;

    BufferedOutputStream fileOutput;
    BufferedInputStream fileInput;


    public FileTransferProtocolClass(Socket connectedClientSocket){
        try {

                clientCommunicationSocket = connectedClientSocket;
                clientCommunicationDataInput = new DataInputStream(clientCommunicationSocket.getInputStream());
                clientCommunicationDataOutput = new DataOutputStream(clientCommunicationSocket.getOutputStream());
                System.out.println("Connected with FTP client...");
                start();

        }
        catch(Exception ex){
        }
    }


    void SendFile() throws Exception
    {
        ServerSocket transferServer = new ServerSocket(1200);
        String filename= clientCommunicationDataInput.readUTF();
        File f=new File(filename);
        if(!f.exists())
        {
            clientCommunicationDataOutput.writeUTF("File Not Found");
            return;
        }
        else
        {
            clientCommunicationDataOutput.writeUTF("READY");

            Socket clientTransferSocket = transferServer.accept(); //Połączenie z socketem na porcie 1200

            fileOutput = new BufferedOutputStream(clientTransferSocket.getOutputStream());
            FileInputStream fin=new FileInputStream(f);
            fileInput = new BufferedInputStream(fin);

            byte[] buffer = new byte[2048];
            int i;

            while ((i = fileInput.read(buffer)) != -1){
                fileOutput.write(buffer,0,i);
            }


            fileOutput.flush();
            fileOutput.close();
            fileInput.close();
            fin.close();
            transferServer.close();
            clientTransferSocket.close();
            clientCommunicationDataOutput.writeUTF("File Was Received Successfully");
        }
    }


    void ReceiveFile() throws Exception
    {
        ServerSocket transferServer = new ServerSocket(1200);

        String filename= clientCommunicationDataInput.readUTF();
        if(filename.compareTo("File not found")==0)
        {
            return;
        }
        File file=new File(filename);
        String option;

        if(file.exists())
        {
            clientCommunicationDataOutput.writeUTF("File Already Exists");
            option= clientCommunicationDataInput.readUTF();
        }
        else
        {
            clientCommunicationDataOutput.writeUTF("SendFile");
            option="Y";
        }

        if(option.compareTo("Y")== 0)
        {
            String fileFullPath = clientCommunicationDataInput.readUTF();

            Socket transferSocket = transferServer.accept();

            fileInput = new BufferedInputStream(transferSocket.getInputStream());
            FileOutputStream fout = new FileOutputStream(fileFullPath);
            fileOutput = new BufferedOutputStream(fout);

            int i;

            while((i = fileInput.read()) != -1){
                fileOutput.write(i);
            }

            fileOutput.close();
            fileInput.close();
            transferServer.close();
            transferSocket.close();

            fout.close();
            clientCommunicationDataOutput.writeUTF("File Was Sent Successfully");

            transferServer.close();
        }
        else
        {
            return;
        }

    }

    private void printExceptionHelper(){
        try{
            clientCommunicationDataOutput.writeUTF("No such file or directory");
        }
        catch(IOException ex){}
    }

    public void DeleteFile(){
        try {
            System.out.println("Usuwam plik...");
            String filePath = clientCommunicationDataInput.readUTF();
            Path pathToDelete = Paths.get(filePath);
            Files.delete(pathToDelete);
            clientCommunicationDataOutput.writeUTF("DELETED");

        }
        catch (NoSuchFileException x) {
            printExceptionHelper();
        }
        catch (IOException InputOutputException){
            System.out.println(InputOutputException);
        }
    }


    public void run(){
        try {
            while (true) {
                System.out.println("Waiting for orders...");
                String order = clientCommunicationDataInput.readUTF();
                switch (order) {
                    case "RETR": {
                        System.out.println("\tCaught RETR comand...");
                        SendFile();
                    }
                    break;
                    case "APPE":
                    {
                        System.out.println("\tCaught APPE comand...");
                        ReceiveFile();
                    }
                    break;
                    case "DELE":
                    {
                        System.out.println("\tCought DELE comand...");
                        DeleteFile();
                    }
                    break;
                    case "QUIT":
                    {
                        System.out.println("\tCaught QUIT order...");
                        System.exit(1);
                    }
                    break;
                }
            }
            }
        catch(Exception ex) {
                System.out.println(ex);
        }
        }
    }