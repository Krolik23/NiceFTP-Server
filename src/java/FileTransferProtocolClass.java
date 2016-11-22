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

    Socket clientTransferSocket;
    DataInputStream clientTransferDataInput;
    DataOutputStream clientTransferDataOutput;


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

    public void setTransferSocket(Socket transferSocket){
        try {
            clientTransferSocket = transferSocket;
            clientTransferDataInput = new DataInputStream(clientTransferSocket.getInputStream());
            clientTransferDataOutput = new DataOutputStream(clientTransferSocket.getOutputStream());
        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }

    public void closeTransferSocket(Socket transferSocket){
        try{
            clientTransferDataInput.close();
            clientTransferDataOutput.close();
            transferSocket.close();
        }
        catch(Exception ex){
            System.out.println(ex);
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

            setTransferSocket(transferServer.accept()); //nasłuchiwanie na porcie 1200

            FileInputStream fin=new FileInputStream(f);
            int ch;
            do
            {
                ch=fin.read();
                clientTransferDataOutput.writeUTF(String.valueOf(ch));
            }
            while(ch!=-1);
            fin.close();
            clientCommunicationDataOutput.writeUTF("File Was Received Successfully");
            closeTransferSocket(clientTransferSocket);
            transferServer.close();
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

            setTransferSocket(transferServer.accept()); //połączenie na porcie 1200
            FileOutputStream fout = new FileOutputStream(fileFullPath);
            int ch;
            String temp;
            do
            {
                temp= clientTransferDataInput.readUTF();
                ch=Integer.parseInt(temp);
                if(ch!=-1)
                {
                    fout.write(ch);
                }
            }while(ch!=-1);
            fout.close();
            clientCommunicationDataOutput.writeUTF("File Was Sent Successfully");
            closeTransferSocket(clientTransferSocket);
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
            clientCommunicationDataOutput.writeUTF("DELATED");

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



