import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 11/24/16.
 */
public class Client {
    public static final String[] SecurityFlag = new String[] {"CONFIDENTIAL", "INTEGRITY", "NONE"};
    private static final String[] PROPAGATION_FLAG = new String[] {"true", "false"};
    private static SSLSocket Ssl_Socket = null;
    private static BufferedReader reader = null;
    private static DataInputStream Data_Input = null;
    private static DataOutputStream Data_Out = null;
    private static PrintWriter Writer = null;
    private static String clientName = null;
    private static ArrayList<String> fileFromServer;
    private static String client_keystore =null;
    private static String username;
    private static String password;
    public static void main(String[] arg) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException, UnrecoverableKeyException {
        //initialization

        boolean quit = false;

        username = get_username();
        password = get_password();

        Scanner user_input = new Scanner(System.in);
        Init_certificate(username);

        Collection<String> security_flags = new ArrayList<String>();
        Collection<String> propagation_flag = new ArrayList<String>();
        for(String s : SecurityFlag) {security_flags.add(s);}
        for(String s : PROPAGATION_FLAG) {propagation_flag.add(s);}


        while (!quit) {
            System.out.print(" Secure_Shared_Store# ");
            String command = user_input.nextLine();
            String[] input = command.split(" ");
            if (input != null) {
                switch (input[0]) {
                    case "start-session":{
                        //example command:
                        //start-session localhost
                        if(input.length != 2) {
                            System.out.println("Incorrect usage of " + input[0]);
                        } else if(Ssl_Socket != null) {
                            System.out.println("Please exit your current secure session before initiating a new one.");
                        } else {
                            start_session(input[1]);
                        }
                        break;
                    }
                    case "check_out": {
                        break;
                    }
                    case "check_in": {
                        break;
                    }
                    case "delegate": {
                        break;
                    }
                    case "safe_delete": {
                        break;
                    }
                    case "quit":
                    case "end-session": {
                        end_session();
                        if (input[0].equals("quit")) {
                            quit = true;
                            System.out.println("Bye!");
                        }
                        break;
                    }
                    case "help": {
                        break;
                    }
                    case "verbose": {
                        break;
                    }
                    case "": {
                        break;
                    }
                    case "shutdown": {
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        }
    }
    private static void Init_certificate(String name) {
        switch(name)
        {
            case "Luoyin1":{
                client_keystore="client.jks";
                clientName ="client";
                break;
            }
            case "Luoyin2":{
                client_keystore="client2.jks";
                clientName ="client2";
                break;
            }
            case "Luoyin3":{
                client_keystore="client3.jks";
                clientName ="client3";
                break;
            }
        }
        System.setProperty("javax.net.ssl.keyStore", client_keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", client_keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
    }
    private static void start_session(String host) {
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        //SSLSession session = null;
        try {
            // Initiate connection
            Ssl_Socket = (SSLSocket) socketFactory.createSocket(host, 8888);
            Writer = new PrintWriter(Ssl_Socket.getOutputStream());
            Data_Input = new DataInputStream(Ssl_Socket.getInputStream());
            Data_Out = new DataOutputStream(Ssl_Socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(Ssl_Socket.getInputStream()));

            //sending Client_name to server for verification if keystore contains alias
            Writer.write(clientName+"\n");
            Writer.flush();

            // Starting handshake with server
            Ssl_Socket.startHandshake();
            String conn = reader.readLine();

            if (conn.equals("Untrusted connection")) {
                System.out.println("Untrusted client");
                end_session();
            } else if (conn.equals("connected successfully"))  {
                System.out.println("Successfully connected to server"+"("+Ssl_Socket.getRemoteSocketAddress()+")"+"!");
            }

            String path = new File(".").getCanonicalPath();
            File directory = new File(path + "/" + clientName);
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            fileFromServer = new ArrayList<String>();

        } catch (IOException e) {
        }

    }
    private void check_out() {

    }
    private static void check_in(String UID, String securityFlag) {

    }
    private void delegate() {

    }
    private void safe_delete() {

    }
    private static void end_session() throws IOException {
        //writeback changed file, if have rights to update
        if(!fileFromServer.isEmpty()) {
            for(int i = 0; i < fileFromServer.size(); i++) {
                if((fileFromServer.get(i)).toString().indexOf(',') < 0) break;
                String[] temp = fileFromServer.get(i).split(",");
                System.out.println("Printing filesFromServer" + Arrays.toString(temp));

                String path = new File(".").getCanonicalPath().toString() + File.separator + clientName +  File.separator  + temp[0];

                check_in(path, temp[1]);
                fileFromServer.remove(temp);
            }
        }

        // Send command to server
        Writer.write("terminate-session\n");
        Writer.flush();

        // Attempt to close the communication socket
        try {
            Ssl_Socket.close();
            reader.close();
            Data_Input.close();
            Writer.close();
        } catch(IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
            return;
        }

        // Set fields to null since they are invalidated now
        Ssl_Socket = null;
        reader = null;
        Writer = null;
    }




    private static String get_username() throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.printf("Username:");
        String username = inFromUser.readLine();

        return username;
    }
    private static String get_password() throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.printf("Password:");
        String password =  inFromUser.readLine();
        return password;
    }
}
