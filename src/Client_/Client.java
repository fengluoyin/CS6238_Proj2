package Client_;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 11/24/16.
 */
public class Client {
    public static final String[] SecurityFlag = new String[] {"CONFIDENTIALITY", "INTEGRITY", "NONE"};
    public static final String[] PROPAGATION_FLAG = new String[] {"true", "false"};
    public static SSLSocket Ssl_Socket = null;
    public static BufferedReader reader = null;
    public static DataInputStream Data_Input = null;
    public static DataOutputStream Data_Out = null;
    public static PrintWriter Writer = null;
    public static String clientName = null;
    public static ArrayList<String> fileFromServer;
    public static String client_keystore =null;
    public static String username;
    public static String password;
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
            System.out.print("Secure_Shared_Store# ");
            String command = user_input.nextLine();
            String[] input = command.split(" ");
            if (input != null) {
                switch (input[0]) {
                    case "start-session":{
                        //example command:
                        //start-session localhost
                        if(input.length != 2) {
                            System.out.println("Incorrect usage of " + input[0]);
                            Help();
                        } else if(Ssl_Socket != null) {
                            System.out.println("Please exit your current secure session before initiating a new one.");
                        } else {
                            Strat_session.start_session(input[1]);
                        }
                        break;
                    }
                    case "check_out": {
                        //example command:
                        //check_out filename
                        if(input.length != 2) {
                            System.out.println("Incorrect usage of " + input[0]);
                            Help();
                        } else if(Ssl_Socket == null) {
                            System.out.println("You must start a session to retrieve a document.");
                        } else {
                            Check.check_out(input[1]);
                        }
                        break;
                    }
                    case "check_in": {
                        //example command:
                        //check_in filename security_flag
                        if(input.length != 3) {
                            System.out.println("Incorrect usage of " + input[0]);
                            Help();
                        } else if(Ssl_Socket == null) {
                            System.out.println("You must start a session to upload a document.");
                        } else {
                            File f = new File(input[1]);
                            if(!f.exists()) {
                                System.out.println("Document " + input[1] + " does not exist.");
                            } else if(!security_flags.contains(input[2])) {
                                System.out.println("Invalid security flag: " + input[2]);
                            } else {
                                Check.check_in(input[1], input[2]);
                            }
                        }
                        break;
                    }
                    case "delegate": {
                        //example command:
                        //delegate test1.txt Luoyin2 30 false
                        if(input.length != 5) {
                            System.out.println("Incorrect usage of " + input[0]);
                            Help();
                        } else if(Ssl_Socket == null) {
                            System.out.println("You must start a session to retrieve a document.");
                        } else {
                            Delegation.delegate(input[1], input[2], input[3], input[4]);
                        }
                        break;
                    }
                    case "safe_delete": {
                        if(input.length != 2) {
                            System.out.println("Incorrect usage of " + input[0]);
                            Help();
                        } else if(Ssl_Socket == null) {
                            System.out.println("You must start a session to retrieve a document.");
                        } else {
                            Safe_delete.safe_delete(input[1]);
                        }
                        break;
                    }
                    case "quit":
                    case "end-session": {
                        End_session.end_session();
                        if (input[0].equals("quit")) {
                            quit = true;
                            System.out.println("Bye!");
                        }
                        break;
                    }
                    case "h":
                    case "help": {
                        Help();
                        break;
                    }
                    default: {
                        System.out.println("Invalid command: " + input[0]);
                        Help();
                        break;
                    }
                }
            }
        }
    }

    private static void Help(){
        System.out.println("\n Secure Shared Store");
        System.out.println("##############################################################");
        System.out.println("commands and arguments ");
        System.out.println("\tstart-session hostname                                   - Start a new session");
        System.out.println("\tcheck_out DocumentUID                                    - Get file");
        System.out.println("\tcheck_in DocumentUID SecurityFlag                        - Push file");
        System.out.println("\tdelegate DocumentUID Client Time PropagationFlag         - Doing Delegation");
        System.out.println("\tsafe_delete DocumentUID                                  - Doing Delegation");
        System.out.println("\tend-session                                              - end the session.");
        System.out.println("##############################################################");
        System.out.println();
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
