import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;


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
                clientCommunicationDataOutput.writeUTF("220 Service ready for new user.");
                start();

        }
        catch(Exception ex){
        }
    }


    void sendFile() throws Exception
    {
        listDir();
        ServerSocket transferServer = new ServerSocket(1200);
        String filename= clientCommunicationDataInput.readUTF();
        File f=new File(filename);
        if(!f.exists())
        {
            clientCommunicationDataOutput.writeUTF(" File Not Found");
            clientCommunicationDataOutput.writeUTF(" 552 Requested file action aborted");
            return;
        }
        else
        {
            clientCommunicationDataOutput.writeUTF(" 150 OK");

            String sendingOption = clientCommunicationDataInput.readUTF();

            if(sendingOption.compareTo("552 Requested file action aborted") == 0){
                return;
            }

            Socket clientTransferSocket = transferServer.accept(); //Połączenie z socketem na porcie 1200
            fileOutput = new BufferedOutputStream(clientTransferSocket.getOutputStream());
            FileInputStream fin=new FileInputStream(f);
            fileInput = new BufferedInputStream(fin);

            byte[] buffer = new byte[2048];
            int i;

            clientCommunicationDataOutput.writeUTF(" 125 Data connection already open; transfer starting");

            while ((i = fileInput.read(buffer)) != -1){
                fileOutput.write(buffer,0,i);
            }


            fileOutput.flush();
            fileOutput.close();
            fileInput.close();
            fin.close();
            transferServer.close();
            clientTransferSocket.close();
            clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed");
        }
    }


    void receiveFile() throws Exception
    {
        ServerSocket transferServer = new ServerSocket(1200);

        String filename= clientCommunicationDataInput.readUTF();
        if(filename.compareTo("File not found")==0)
        {
            clientCommunicationDataOutput.writeUTF(" 552 Requested file action aborted");
            return;
        }
        File file=new File(filename);
        String option;

        if(file.exists())
        {
            clientCommunicationDataOutput.writeUTF(" 450 Requested file action not taken; File Already Exists");
            option= clientCommunicationDataInput.readUTF();
        }
        else
        {
            clientCommunicationDataOutput.writeUTF(" 150 File status okay; about to open data connection");

            option="Y";
        }

        if(option.compareTo("Y")== 0)
        {

            String fileFullPath = clientCommunicationDataInput.readUTF();

            Socket transferSocket = transferServer.accept();


            fileInput = new BufferedInputStream(transferSocket.getInputStream());
            FileOutputStream fout = new FileOutputStream(fileFullPath);
            fileOutput = new BufferedOutputStream(fout);
            clientCommunicationDataOutput.writeUTF(" 125 Data connection already open; transfer starting.");

            int i;

            while((i = fileInput.read()) != -1){
                fileOutput.write(i);
            }



            fileOutput.close();
            fileInput.close();
            transferServer.close();
            transferSocket.close();

            fout.close();
            clientCommunicationDataOutput.writeUTF(" 226 Closing data connection.Requested file action successful");


            transferServer.close();
            clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed.");
        }
        else
        {
            return;
        }

    }

    private void printExceptionHelper(){
        try{
            clientCommunicationDataOutput.writeUTF("!!! No such file or directory !!!");
        }
        catch(IOException ex){}
    }

    public void deleteFile() throws Exception{
        try {
            System.out.println("Usuwam plik...");
            String filePath = clientCommunicationDataInput.readUTF();
            Path pathToDelete = Paths.get(filePath);
            Files.delete(pathToDelete);
            clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed");

        }
        catch (NoSuchFileException x) {
            clientCommunicationDataOutput.writeUTF(" 550 Requested action not taken; file not found");
            printExceptionHelper();
        }
        catch(DirectoryNotEmptyException x){
            clientCommunicationDataOutput.writeUTF(" 550 Requested action not taken; directory not empty");
        }
        catch (IOException InputOutputException){
            System.out.println(InputOutputException);
        }
    }

    public void checkUsername(String userName) throws Exception{
        if(userName.compareTo("szymez") == 0){
            clientCommunicationDataOutput.writeUTF("331 User name okay, need password");
        }
        else{
            clientCommunicationDataOutput.writeUTF("530 Not logged in");
        }
    }

    public void checkPassword(String password) throws Exception{
        if(password.compareTo("szymon") == 0){
            clientCommunicationDataOutput.writeUTF("230 User logged in, proceed");
        }
        else{
            clientCommunicationDataOutput.writeUTF("530 Not logged in");
        }
    }

    public void listDir() throws Exception{
        String dirPath = "C:/Users/Królik/IdeaProjects/NiceFTP-Server/FTPServer";
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        ObjectOutputStream out = new ObjectOutputStream(clientCommunicationDataOutput);
        out.writeObject(files);

    }


    public void run(){
        try {
            while (true) {
                System.out.println("Waiting for orders...");
                String order = clientCommunicationDataInput.readUTF();
                switch (order) {
                    case "RETR": {
                        System.out.println("\tCaught RETR comand...");
                        sendFile();
                    }
                    break;
                    case "APPE":
                    {
                        System.out.println("\tCaught APPE comand...");
                        receiveFile();
                    }
                    break;
                    case "DELE":
                    {
                        System.out.println("\tCought DELE comand...");
                        deleteFile();
                    }
                    break;
                    case "USER":
                    {
                        String userName = clientCommunicationDataInput.readUTF();
                        checkUsername(userName);
                    }
                    break;
                    case "PASS":
                    {
                        String password = clientCommunicationDataInput.readUTF();
                        checkPassword(password);
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