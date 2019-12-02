import java.io.*;
import java.net.*;
import java.util.*;

public class MultiServer implements Runnable{
    //Member
    private Socket socket;
    private Socket fileSocket;
    private ArrayList<MultiServerThread> list;
    private ArrayList<FileServerThread> fstList;
    private ArrayList<String> idList;
    private ArrayList<String> fileList;
    private ServerSocket ss;
    private ServerSocket fss;

    //Constructor
    public MultiServer() {
        System.out.println("Normal Server Start");
        list = new ArrayList<MultiServerThread>();
        fstList = new ArrayList<FileServerThread>();
        idList = new ArrayList<String>();
        fileList = findFileList();
    }

    @Override
    public void run() {
        boolean isStop = false;
        try {
            ServerSocket ss = new ServerSocket(8000);   //---1
            ServerSocket fss = new ServerSocket(9000);
            MultiServerThread mst = null;
            FileServerThread fst = null;
            while(!isStop) {
                System.out.println("Normal Server Read...");
                socket = ss.accept();   //Waiting Connect---2
                fileSocket = fss.accept();
                mst = new MultiServerThread(this);
                fst = new FileServerThread(this);
                list.add(mst);  //---3
                fstList.add(fst);
                Thread t = new Thread(mst);
                Thread ft = new Thread(fst);
                t.start();  //---4
                ft.start();
            }
        } catch(IOException ioe) { ioe.printStackTrace(); }
    }

    //Method
    public ArrayList<String> findFileList() {
        ArrayList<String> result = new ArrayList<String>();
        String path = System.getProperty("user.dir");
        File dir = new File(path);
        String files[]  = dir.list();
        for (String fn: files)
            result.add(fn);
        return result;
    }

    public Socket getSocket() { return socket; }

    public Socket getFileSocket() { return fileSocket; }

    public ArrayList<MultiServerThread> getList() { return list; }

    public ArrayList<FileServerThread> getFstList() { return fstList; }

    public ArrayList<String> getIdList() { return idList; }

    public ArrayList<String> getFileList() { return fileList; }

    public void setFileList(ArrayList<String> fileList) { this.fileList = fileList; }
}
