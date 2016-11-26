package Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 11/26/16.
 */
public class Delegatation {
    public static void delegate(String UID, String originalClient, String clientToAddPermission, String time, String propogationFlag,Server server)
    {
        try
        {
            String filename = UID;
            String folderName = filename.substring(0, filename.indexOf('.'));
            String path = new File(".").getCanonicalPath();
            File directory = new File(path + "/File_system/" + folderName);
            //File does not exist
            if (!directory.exists())
            {
                server.Writer.write("DOES_NOT_EXIST\n");
                server.Writer.flush();
                return;
            }

            File metaData = new File(path + "/File_system/" + folderName + "/metadata.txt");
            boolean authorized = false;
            Scanner metaDataStream = new Scanner(metaData);
            String owner = metaDataStream.nextLine();
            String securityFlag = metaDataStream.nextLine();
            if (owner.equals(originalClient))
            {
                authorized = true;
            }
            ArrayList<String> allClients = new ArrayList<String>();
            long maxTime = Long.MAX_VALUE;
            while (metaDataStream.hasNextLine())
            {
                String current = metaDataStream.nextLine();
                String[] entries = current.split(",");
                allClients.add(entries[0]);
                if (entries[0].equals(originalClient))
                {
                    if (entries[3].equals("true"))
                    {
                        Date currentTime = new Date();
                        Date permissionTime = new Date(Long.parseLong(entries[2]));
                        if (currentTime.before(permissionTime))
                        {

                            authorized = true;
                            maxTime = Long.parseLong(entries[2]);

                        }
                    }
                }
            }
            metaDataStream.close();
            if (!authorized)
            {
                server.Writer.write("NOT_AUTHORIZED\n");
                server.Writer.flush();
                return;
            }

            Date timeNow = new Date();
            long finalTimeToAdd = Math.min(maxTime, timeNow.getTime() + (Long.parseLong(time) * 1000));
            String timeToAdd = String.valueOf(finalTimeToAdd);
            if (clientToAddPermission.equals("ALL"))
            {
                FileWriter fw = new FileWriter(metaData.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(originalClient);
                bw.close();

                fw = new FileWriter(metaData.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write("\n" + securityFlag);

                for (String currentClient : allClients)
                {
                    String toWrite = "\n" + currentClient + ',' + timeToAdd + ',' + propogationFlag;
                    bw.write(toWrite);
                }

                bw.close();
            }
            else
            {
                String toWrite = "\n" + clientToAddPermission +  ',' + timeToAdd + ',' + propogationFlag;

                FileWriter fw = new FileWriter(metaData.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(toWrite);
                bw.close();
            }
            server.Writer.write("DELEGATED\n");
            server.Writer.flush();
            return;

        } catch (Exception e) {
            System.out.println("Exception while checking out file");
            e.printStackTrace();
        }
    }
}
