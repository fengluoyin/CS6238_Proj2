package Client_;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class End_session {
    public static void end_session() throws IOException {
        //writeback changed file, if have rights to update
        if(!Client.fileFromServer.isEmpty()) {
            for(int i = 0; i < Client.fileFromServer.size(); i++) {
                if((Client.fileFromServer.get(i)).toString().indexOf(',') < 0) break;
                String[] temp = Client.fileFromServer.get(i).split(",");
                System.out.println("Printing filesFromServer" + Arrays.toString(temp));

                String path = new File(".").getCanonicalPath().toString() + File.separator + Client.clientName +  File.separator  + temp[0];

                Check.check_in(path, temp[1]);
                Client.fileFromServer.remove(temp);
            }
        }

        // Send command to server
        Client.Writer.write("terminate-session\n");
        Client.Writer.flush();

        // Attempt to close the communication socket
        try {
            Client.Ssl_Socket.close();
            Client.reader.close();
            Client.Data_Input.close();
            Client.Writer.close();
        } catch(IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
            return;
        }

        // Set fields to null since they are invalidated now
        Client.Ssl_Socket = null;
        Client.reader = null;
        Client.Writer = null;
    }

}
