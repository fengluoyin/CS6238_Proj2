package Client_;

import java.io.IOException;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class Safe_delete {
    public static void safe_delete(String Document_UID) {
        try {
            // Send command to server
            Client.Writer.write("safe_delete\n" + Document_UID + ',' + Client.clientName + '\n');
            Client.Writer.flush();
            Client.fileFromServer.add(Document_UID);
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
            else if ("DELETED".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" deleted.");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
