import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;

public class FileInfo implements Serializable{

    private String fileName;
    private String fileAbsolutePath;
    private long fileLength;

    FileInfo(String fileName, long fileLength, String fileAbsolutePath){
        this.fileName = fileName;
        this.fileAbsolutePath = fileAbsolutePath;
        this.fileLength = fileLength;

    }

    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(fileName);
        stream.writeObject(fileAbsolutePath);
        stream.writeLong(fileLength);
    }

    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        fileName = (String) stream.readObject();
        fileAbsolutePath = (String) stream.readObject();
        fileLength = stream.readLong();
    }


    String getFileName(){
        return fileName;
    }

    String getFileAbsolutePath(){
        return fileAbsolutePath;
    }

    long getFileLength(){
        return fileLength;
    }


}
