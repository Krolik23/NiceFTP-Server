import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


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


    void SendFile() throws Exception
    {
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
            FileInputStream fin=new FileInputStream(f);
            int ch;
            do
            {
                ch=fin.read();
                clientCommunicationDataOutput.writeUTF(String.valueOf(ch));
            }
            while(ch!=-1);
            fin.close();
            clientCommunicationDataOutput.writeUTF("File Receive Successfully");
        }
    }


    void ReceiveFile() throws Exception
    {
        ServerSocket transferServer = new ServerSocket(4888);

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

        if(option.compareTo("Y")== 0)    //Tutaj dodać gdzie zapisać plik
        {
            setTransferSocket(transferServer.accept());

            FileOutputStream fout = new FileOutputStream("C:/Users/Królik/Desktop/InstaLoader/pliczek.exe");
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
            clientCommunicationDataOutput.writeUTF("File Send Successfully");
            transferServer.close();
        }
        else
        {
            return;
        }

    }


    public void run(){
        try {
            while (true) {
                System.out.println("Waiting for orders...");
                    String order = clientCommunicationDataInput.readUTF();
                switch (order) {
                    case "RECEIVE": {
                        System.out.println("\tCaught RECEIVE order...");
                        SendFile();
                    }
                    break;
                    case "SEND":
                    {
                        System.out.println("\tCaught SEND order...");
                        ReceiveFile();
                    }
                    break;
                    case "DISCONNECT":
                    {
                        System.out.println("\tCaught DISCONNECT order...");
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



