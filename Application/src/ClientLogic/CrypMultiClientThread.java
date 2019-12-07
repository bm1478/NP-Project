package ClientLogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CrypMultiClientThread implements Runnable {
    //Member
    private MultiClient mc;

    //Constructor
    public CrypMultiClientThread(MultiClient mc) {
        this.mc = mc;
        try {
            mc.setIdarr((ArrayList<String>)mc.getOis().readObject());
        } catch(Exception e) { e.printStackTrace(); }
    }

    //Method
    @Override
    public void run() {
        String msg = null;
        String[] receive = null;
        boolean isStop = false;
        while(!isStop) {
            try {
                msg = (String)mc.getOis().readObject();
                receive = msg.split("#");
            } catch(Exception e) {
                e.printStackTrace();
                isStop = true;
            }
            if(receive[1].equals("quit")) { //quit msg
                if(receive[0].equals(mc.getId())) {
                    mc.getIdarr().remove(receive[0]);
                    updatePKList();
                    mc.getJf().setVisible(false);
                    System.exit(0);
                }
                else {
                    mc.getJta().append("Client " + receive[0] +" terminates" + System.getProperty("line.separator"));
                    mc.getJta().setCaretPosition(mc.getJta().getDocument().getLength());
                    mc.getIdarr().remove(receive[0]);
                    updateIDList();
                    updatePKList();
                }
            }
            else if(receive[1].equals("Enter")) {
                mc.getJta().append("Client " + receive[0] + " Come in" + System.getProperty("line.separator"));
                mc.getJta().setCaretPosition(mc.getJta().getDocument().getLength());
                mc.getIdarr().add(receive[0]);
                updateIDList();
                updatePKList();
                if (mc.getIdarr().size() == 1) {
                    String key = mc.getCaes().createRandomKey();    //Create AES Key
                    String encryptedKey = mc.getCrsa().encode(key, mc.getPrivateKey()); //RSA Encrypt AES Key
                    mc.setChatAESKey(key);
                    mc.setFirstUser(mc.getId());
                    try {
                        mc.getOos().writeObject(mc.getId() + "#" + encryptedKey);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    System.out.println(mc.getId() + " " + mc.getChatAESKey());
                }
                else {
                    if (mc.getFirstUser().equals("")) {
                        try {
                            String idAndKey = (String) mc.getOis().readObject();
                            String[] idKey = idAndKey.split("#");
                            mc.setFirstUser(idKey[0]);
                            String stringPublicKey = (String) mc.getOis().readObject();
                            mc.setChatAESKey(mc.getCrsa().decode(idKey[1], stringPublicKey));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(mc.getId() + " " + mc.getChatAESKey());
                    }
                }
            }
            else {  //normal msg
                String result = "";
                try {
                    mc.getCaes().createKey(mc.getChatAESKey());
                    mc.getCaes().modeDecrypt();
                    result = mc.getCaes().msgAESDecrypt(receive[1]);
                } catch(Exception e) { e.printStackTrace(); }
                mc.getJta().append(receive[0] + " : " + result + System.getProperty("line.separator"));
                mc.getJta().setCaretPosition(mc.getJta().getDocument().getLength());
            }
        }
    }

    public void updateIDList() {
        mc.getIdList().setText(" Chatting List ");
        for (String str : mc.getIdarr()) {
            mc.getIdList().append(System.getProperty("line.separator") + " " + str);
            mc.getIdList().setCaretPosition(mc.getIdList().getDocument().getLength());
        }
    }

    public void updatePKList() {
        String[] idarr = mc.getIdarr().toArray(new String[mc.getIdarr().size()]);
        mc.setPublicKeyList(new HashMap<String, String>());
        try {
            ArrayList<String> arr = new ArrayList<String>();
            for(int i=0; i<idarr.length;i++) {
                String pk = (String)mc.getOis().readObject();
                arr.add(pk);
            }
            String[] pkList = arr.toArray(new String[arr.size()]);
            for (int i=0;i<idarr.length;i++) {
                mc.getPublicKeyList().put(idarr[i], pkList[i]);
            }
        } catch(ClassNotFoundException | IOException e) { e.printStackTrace(); }
    }

}