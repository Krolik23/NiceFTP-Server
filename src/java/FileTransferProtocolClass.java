import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;


public class FileTransferProtocolClass extends Thread{

    //String home = System.getProperty("user.home")+"\\Desktop\\NiceFTP-Server\\FTPServer\\";
    String home = System.getProperty("user.home") + "\\IdeaProjects\\NiceFTP-Server\\FTPServer\\";

    Socket clientCommunicationSocket;
    InputStreamReader clientCommunicationDataInput;
    OutputStreamWriter clientCommunicationDataOutput;

    BufferedReader commandReader;

    BufferedOutputStream fileOutput;
    BufferedInputStream fileInput;


    public FileTransferProtocolClass(Socket connectedClientSocket){
        try {

                clientCommunicationSocket = connectedClientSocket;
                clientCommunicationDataInput = new InputStreamReader(clientCommunicationSocket.getInputStream());
                commandReader = new BufferedReader(clientCommunicationDataInput);
                clientCommunicationDataOutput = new OutputStreamWriter(clientCommunicationSocket.getOutputStream(),"UTF-8");
                System.out.println("Connected with FTP client...");
                //clientCommunicationDataOutput.writeUTF("220 Service ready for new user.");
                clientCommunicationDataOutput.write("220 Service ready for new user.\n",0,"220 Service ready for new user.\n".length());
                clientCommunicationDataOutput.flush();
                start();

        }
        catch(Exception ex){
            System.out.println(ex);
        }
    }


    void sendFile() throws Exception
    {
        if(listDir() == 1){
            return;
        }
        ServerSocket transferServer = new ServerSocket(1200);
        Socket clientTransferSocket = transferServer.accept(); //Połączenie z socketem na porcie 1200
        String filename= commandReader.readLine();
        File f=new File(filename);
        if(!f.exists())
        {
            //clientCommunicationDataOutput.writeUTF(" File Not Found");
            clientCommunicationDataOutput.write(" File Not Found\n",0," File Not Found\n".length());
            clientCommunicationDataOutput.flush();
            //clientCommunicationDataOutput.writeUTF(" 552 Requested file action aborted");
            clientCommunicationDataOutput.write(" 552 Requested file action aborted\n",0," 552 Requested file action aborted\n".length());
            clientCommunicationDataOutput.flush();
            return;
        }
        else
        {
            //clientCommunicationDataOutput.writeUTF(" 150 OK");
            clientCommunicationDataOutput.write(" 150 OK\n",0," 150 OK\n".length());
            clientCommunicationDataOutput.flush();

            //String sendingOption = clientCommunicationDataInput.readUTF();
            String sendingOption = commandReader.readLine();

            if(sendingOption.compareTo("552 Requested file action aborted") == 0){
                clientTransferSocket.close();
                transferServer.close();
                return;
            }


            fileOutput = new BufferedOutputStream(clientTransferSocket.getOutputStream());
            FileInputStream fin=new FileInputStream(f);
            fileInput = new BufferedInputStream(fin);

            byte[] buffer = new byte[2048];
            int i;

            //clientCommunicationDataOutput.writeUTF(" 125 Data connection already open; transfer starting");
            clientCommunicationDataOutput.write(" 125 Data connection already open; transfer starting\n",0," 125 Data connection already open; transfer starting\n".length());
            clientCommunicationDataOutput.flush();

            while ((i = fileInput.read(buffer)) != -1){
                fileOutput.write(buffer,0,i);
            }
            fileOutput.flush();


            fileOutput.close();
            fileInput.close();
            fin.close();
            transferServer.close();
            clientTransferSocket.close();
            //clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed");
            clientCommunicationDataOutput.write(" 250 Requested file action okay, completed\n",0," 250 Requested file action okay, completed\n".length());
            clientCommunicationDataOutput.flush();
        }
    }


    void receiveFile() throws Exception
    {
        ServerSocket transferServer = new ServerSocket(1200);
        Socket transferSocket = transferServer.accept();

        //String filename= clientCommunicationDataInput.readUTF();
        String filename= commandReader.readLine();

        if(filename.compareTo("Wrong path") == 0 || filename.compareTo("File not found") == 0)
        {
            if(filename.compareTo("Wrong path") == 0){
                clientCommunicationDataOutput.write("500 Syntax error, wrong path\n",0,"500 Syntax error, wrong path\n".length());
                clientCommunicationDataOutput.flush();
            }else if(filename.compareTo("File not found") == 0){
                clientCommunicationDataOutput.write("550 File not found\n",0,"File not found\n".length());
                clientCommunicationDataOutput.flush();
            }
            clientCommunicationDataOutput.write("552 Requested file action aborted\n",0,"552 Requested file action aborted\n".length());
            clientCommunicationDataOutput.flush();
            transferSocket.close();
            transferServer.close();
            return;
        }
        else if(filename.compareTo("Empty directory") == 0){
            clientCommunicationDataOutput.write("451 Empty local directory\n",0,"451 Empty local directory\n".length());
            clientCommunicationDataOutput.flush();
            clientCommunicationDataOutput.write("552 Requested file action aborted\n",0,"552 Requested file action aborted\n".length());
            clientCommunicationDataOutput.flush();
            transferSocket.close();
            transferServer.close();
            return;
        }
        File file=new File(filename);
        String option;

        if(file.exists())
        {
            //clientCommunicationDataOutput.writeUTF(" 450 Requested file action not taken; File Already Exists");
            clientCommunicationDataOutput.write("450 Requested file action not taken; File Already Exists; Continue anyway?\n",0,"450 Requested file action not taken; File Already Exists; Continue anyway?\n".length());
            clientCommunicationDataOutput.flush();
            option= commandReader.readLine();
        }
        else
        {
            //clientCommunicationDataOutput.writeUTF(" 150 File status okay; about to open data connection");
            clientCommunicationDataOutput.write("150 File status okay; about to open data connection\n",0,"150 File status okay; about to open data connection\n".length());
            clientCommunicationDataOutput.flush();

            option="Y";
        }

        if(option.compareTo("Y")== 0)
        {

            //String fileFullPath = clientCommunicationDataInput.readUTF();
            String fileFullPath = commandReader.readLine();

            //Socket transferSocket = transferServer.accept();


            fileInput = new BufferedInputStream(transferSocket.getInputStream());
            FileOutputStream fout = new FileOutputStream(fileFullPath);
            fileOutput = new BufferedOutputStream(fout);
            //clientCommunicationDataOutput.writeUTF(" 125 Data connection already open; transfer starting.");
            clientCommunicationDataOutput.write("125 Data connection already open; transfer starting.\n",0,"125 Data connection already open; transfer starting.\n".length());
            clientCommunicationDataOutput.flush();

            int i;

            while((i = fileInput.read()) != -1){
                fileOutput.write(i);
            }



            fileOutput.close();
            fileInput.close();
           // transferServer.close();
            transferSocket.close();

            fout.close();
            //clientCommunicationDataOutput.writeUTF(" 226 Closing data connection.Requested file action successful");
            clientCommunicationDataOutput.write("226 Closing data connection.Requested file action successful\n",0,"226 Closing data connection.Requested file action successful\n".length());
            clientCommunicationDataOutput.flush();


            transferServer.close();
            //clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed.");
            clientCommunicationDataOutput.write("250 Completed\n",0,"250 Completed\n".length());
            clientCommunicationDataOutput.flush();

        }
        else
        {
            transferSocket.close();
            transferServer.close();
            return;
        }

    }

    private void printExceptionHelper(){
        try{
            //clientCommunicationDataOutput.writeUTF("!!! No such file or directory !!!");
            clientCommunicationDataOutput.write("!!! No such file or directory !!!\n",0,"!!! No such file or directory !!!\n".length());
            clientCommunicationDataOutput.flush();
        }
        catch(IOException ex){}
    }

    public void deleteFile() throws Exception{
        try {
            listDir();
            System.out.println("Deleting file...");
            //String filePath = clientCommunicationDataInput.readUTF();
            String filePath = commandReader.readLine();

            Path pathToDelete = Paths.get(filePath);
            Files.delete(pathToDelete);
            //clientCommunicationDataOutput.writeUTF(" 250 Requested file action okay, completed");
            clientCommunicationDataOutput.write(" 250 Requested file action okay, completed\n",0," 250 Requested file action okay, completed\n".length());
            clientCommunicationDataOutput.flush();

        }
        catch (NoSuchFileException x) {
            //clientCommunicationDataOutput.writeUTF(" 550 Requested action not taken; file not found");
            clientCommunicationDataOutput.write(" 550 Requested action not taken; file not found\n",0," 550 Requested action not taken; file not found\n".length());
            clientCommunicationDataOutput.flush();
            printExceptionHelper();
        }
        catch(DirectoryNotEmptyException x){
            //clientCommunicationDataOutput.writeUTF(" 550 Requested action not taken; directory not empty");
            clientCommunicationDataOutput.write(" 550 Requested action not taken; directory not empty\n",0," 550 Requested action not taken; directory not empty\n".length());
            clientCommunicationDataOutput.flush();
        }
        catch (IOException InputOutputException){
            System.out.println(InputOutputException);
        }
    }

    public void checkUsername(String userName) throws Exception{
        if(userName.compareTo("test") == 0){
            //clientCommunicationDataOutput.writeUTF("331 User name okay, need password");
            clientCommunicationDataOutput.write("331 User name okay, need password\n",0,"331 User name okay, need password\n".length());
            clientCommunicationDataOutput.flush();
        }
        else{
            //clientCommunicationDataOutput.writeUTF("530 Not logged in");
            clientCommunicationDataOutput.write("530 Not logged in\n",0,"530 Not logged in\n".length());
            clientCommunicationDataOutput.flush();
        }
    }

    public void checkPassword(String password) throws Exception{
        if(password.compareTo("test1") == 0){
            //clientCommunicationDataOutput.writeUTF("230 User logged in, proceed");
            clientCommunicationDataOutput.write("230 User logged in, proceed\n",0,"230 User logged in, proceed\n".length());
            clientCommunicationDataOutput.flush();
        }
        else{
            //clientCommunicationDataOutput.writeUTF("530 Not logged in");
            clientCommunicationDataOutput.write("530 Not logged in\n",0,"530 Not logged in\n".length());
            clientCommunicationDataOutput.flush();
        }
    }

    public int listDir() throws Exception{

        ServerSocket transferServer = new ServerSocket(1200);
        clientCommunicationDataOutput.write("150 File status okay; about to open data connection\n",0,"150 File status okay; about to open data connection\n".length());
        clientCommunicationDataOutput.flush();
        Socket transferSocket = transferServer.accept();



        String dirPath = home;  //C:/Users/Królik/IdeaProjects/NiceFTP-Server/FTPServer
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        FileInfo[] info = new FileInfo[files.length];

        for(int i = 0; i < files.length; i++){
            info[i] = new FileInfo(files[i].getName(),files[i].length(),files[i].getAbsolutePath());
        }


        ObjectOutputStream out = new ObjectOutputStream(transferSocket.getOutputStream());
        out.writeObject(home);
        out.flush();
        out.writeObject(info);
        out.flush();
        String verdict = commandReader.readLine();
        if(verdict.compareTo("Empty directory") == 0){
            clientCommunicationDataOutput.write("450 Requested file action not taken\n",0,"450 Requested file action not taken\n".length());
            clientCommunicationDataOutput.flush();
            transferServer.close();
            return 1;
        }
        else {
            clientCommunicationDataOutput.write("250 Requested file action okay, completed\n", 0, "250 Requested file action okay, completed\n".length());
            clientCommunicationDataOutput.flush();
            transferServer.close();
            return 0;
        }

    }


    public void run(){
        try {
            boolean exit = false;
            while (!exit) {
                System.out.println("Waiting for orders...");
                //String order = clientCommunicationDataInput.readUTF();
                String order = commandReader.readLine();
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
                    case "LIST":
                    {
                        listDir();
                    }
                    break;
                    case "USER":
                    {
                        //String userName = clientCommunicationDataInput.readUTF();
                        String userName = commandReader.readLine();

                        checkUsername(userName);
                    }
                    break;
                    case "PASS":
                    {
                        //String password = clientCommunicationDataInput.readUTF();
                        String password = commandReader.readLine();
                        checkPassword(password);
                    }
                    break;
                    case "QUIT":
                    {
                        System.out.println("\tCaught QUIT order...");
                        clientCommunicationDataOutput.write("221 Thank you for using NiceFTP\n",0,"221 Thank you for using NiceFTP\n".length());
                        clientCommunicationDataOutput.flush();
                        System.exit(0);


                    }
                    break;
                }
            }

            }
        catch(Exception ex) {
                System.out.println(ex);
                System.exit(0);
        }
        }
    }