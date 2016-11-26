package Server;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server extends Thread{
    public Socket socket = null;
    public String username = null;
    public String key_f = null;
    public String keypass = null;
    public BufferedReader reader = null;
    public PrintWriter Writer = null;
    public DataOutputStream Data_Out = null;
    public DataInputStream Data_Input = null;
    boolean End_flag = false;
    public PrivateKey private_Key;
    public PublicKey public_Key;
    public static HashMap<String, MetaData> hashMap = new HashMap<String, MetaData>();

    public Server(SSLSocket Socket, String key_f, String keypass) {
        this.socket = Socket;
        this.key_f = key_f;
        this.keypass = keypass;
    }
    public static void main(String[] args)
    {


        Init_certificate();

        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket ss;
        try {
            ss = (SSLServerSocket) ssf.createServerSocket(6666);
            ss.setNeedClientAuth(true);
            System.out.println("Listening for incoming connections on port 6666");

            String path = new File(".").getCanonicalPath();
            File directory = new File(path + "/File_system");
            if (!directory.exists()) {
                directory.mkdir();
            }

            while(true) {
                new Server((SSLSocket) ss.accept(),"server.jks","password").start();
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);

        }

    }

    public void Connect() throws IOException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
        // Get input and output handles for client communication
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.Writer = new PrintWriter(socket.getOutputStream());
        this.Data_Out = new DataOutputStream(socket.getOutputStream());
        this.Data_Input = new DataInputStream(socket.getInputStream());
        File file = new File(key_f);
        FileInputStream fis = new FileInputStream(file);
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(fis,keypass.toCharArray());

        //Receiving alias from Client
        String alias = reader.readLine();
        username = alias;
        Boolean valid = keystore.containsAlias(alias);
        if (!valid) {
            System.out.println(alias + " not found in trusted store \n Try with another alias");
            Writer.write("Untrusted connection\n");
            Writer.flush();
            End_flag = true;
        } else {
            Writer.write("connected successfully\n");
            Writer.flush();
        }
        private_Key = (PrivateKey) keystore.getKey("server",keypass.toCharArray());
        public_Key = keystore.getCertificate("server").getPublicKey();
    }

    public void run() {
        System.out.println("Connected to user " + username + ":" + socket.getPort() + "!");

        try {
            Connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        try {
            do {
                String command = reader.readLine();
                if(command == null) break;
                switch(command) {
                    case "terminate-session":
                        End_flag = true;
                        break;
                    case "check-in":
                        Check.checkIn(public_Key, private_Key,this);
                        break;
                    case "check-out":
                        Check.checkOut(public_Key,private_Key,this);
                        break;
                    case "delegate":
                        String temp = reader.readLine();
                        String[] commands = temp.split(",");
                        Delegatation.delegate(commands[0], commands[1], commands[2], commands[3], commands[4],this);
                        break;
                    case "safe_delete":
                        Safe_delete.safeDelete(reader.readLine(),this);
                        break;
                    default:
                        System.out.println("Unknown command received: " + command);
                }
            } while(!End_flag);
            reader.close();
            Writer.close();
        } catch(Exception e) {
            System.out.println("Connection to user " + username + " interrupted: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Ended connection with user " + socket.getInetAddress().toString().substring(1) +
                ":" + socket.getPort() + ".");
    }
    public static void Init_certificate(){
        System.setProperty("javax.net.ssl.keyStore", "server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "server.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }



}