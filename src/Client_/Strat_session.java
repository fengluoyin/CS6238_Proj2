package Client_;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class Strat_session {
    public static void start_session(String host) {
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        //SSLSession session = null;
        try {
            // Initiate connection
            Client.Ssl_Socket = (SSLSocket) socketFactory.createSocket(host, 6666);
            Client.Writer = new PrintWriter(Client.Ssl_Socket.getOutputStream());
            Client.Data_Input = new DataInputStream(Client.Ssl_Socket.getInputStream());
            Client.Data_Out = new DataOutputStream(Client.Ssl_Socket.getOutputStream());
            Client.reader = new BufferedReader(new InputStreamReader(Client.Ssl_Socket.getInputStream()));

            //sending Client_name to server for verification if keystore contains alias
            Client.Writer.write(Client.clientName+"\n");
            Client.Writer.flush();

            // Starting handshake with server
            Client.Ssl_Socket.startHandshake();
            String conn = Client.reader.readLine();

            if (conn.equals("Untrusted connection")) {
                System.out.println("Untrusted client");
                End_session.end_session();
            } else if (conn.equals("connected successfully"))  {
                System.out.println("Successfully connected to server"+"("+ Client.Ssl_Socket.getRemoteSocketAddress()+")"+"!");
            }

            String path = new File(".").getCanonicalPath();
            File directory = new File(path + "/" + Client.username);
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }
            Client.fileFromServer = new ArrayList<String>();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
