package Client_;

import java.io.IOException;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class Delegation {
    public static void delegate(String Document_UID, String client, String time, String propogationFlag) {
        try {
            if(client.equals("Luoyin1"))
                client="client";
            if(client.equals("Luoyin2"))
                client="client2";
            if(client.equals("Luoyin3"))
                client="client3";
            // Send command to server
            Client.Writer.write("delegate\n" + Document_UID + ',' + Client.clientName + ',' + client + ',' + time  + ',' + propogationFlag + '\n');
            Client.Writer.flush();

            // Needed for termination. DO NOT DELETE
            Client.fileFromServer.add(Document_UID);


            // Get message from server
            String reply = Client.reader.readLine();
            if("DOES_NOT_EXIST".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" does not exist.");
                return;
            }
            else if ("NOT_AUTHORIZED".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" not authorized.");
                return;
            }
            else if ("DELEGATED".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" delegated.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
