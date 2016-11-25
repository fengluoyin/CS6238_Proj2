package Client_;

import java.io.*;

/**
 * Created by luoyinfeng on 11/25/16.
 */
public class Check {
    public static void check_out(String Document_UID) {
        try {
            // Send command to server
            Client.Writer.write("check-out\n" + Document_UID + ',' + Client.clientName + '\n');
            Client.Writer.flush();

            // Get message from server
            String reply = Client.reader.readLine();
            // Does the file exist?
            if("DOES_NOT_EXIST".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" does not exist.");
                return;
            }
            else if("NOT_AUTHORIZED".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" not authorized.");
                return;
            }
            else if("SIGNATURE_MISMATCH".equals(reply))
            {
                System.out.println("File \"" + Document_UID + "\" signature mismatch.");
                return;
            }
            System.out.println("File \"" + Document_UID +"dasdsa:"+reply);
            // Get the file
            int filesize = Integer.parseInt(reply);
            byte filebytes[] = new byte[filesize];
            int bytesread = 0;
            while(bytesread < filesize) {
                int thisread = Client.Data_Input.read(filebytes, bytesread, filesize - bytesread);
                if(thisread >= 0) {
                    bytesread += thisread;
                } else {
                    System.out.println("Encountered an error while downloading file");
                }
            }

            // Write the file
            File file = new File(Client.username + "/" + Document_UID);
            if(file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(filebytes, 0, filesize);
            bos.flush();
            bos.close();
            fos.close();

            String securityFlag = Client.reader.readLine();
            Client.fileFromServer.add(Document_UID + "," + securityFlag);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void check_in(String Document_UID, String securityFlag) {
        // Call check in procedure at server
        File file = new File(Document_UID);
        System.out.println(file.getName());
        String filename = file.getName();
        Client.Writer.write("check-in\n");
        Client.Writer.flush();

        Client.Writer.write(filename +  ',' + Client.clientName + ',' + securityFlag + "\n");
        Client.Writer.flush();

        try {
            File file1 = new File(Document_UID);
            int size = (int) file1.length();
            //Send file length
            Client.Writer.write(size + "\n");
            Client. Writer.flush();
            //Send file
            byte filebytes[] = new byte[size];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(filebytes, 0, size);
            bis.close();
            fis.close();
            Client.Data_Out.write(filebytes, 0, size);
            Client.Data_Out.flush();
            System.out.println("Successfully sent " + Document_UID);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
