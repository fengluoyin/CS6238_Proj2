package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 11/26/16.
 */
public class Safe_delete {
    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public static void safeDelete(String UID,Server server)
    {
        try
        {
            String[] input = UID.split(",");
            String filename = input[0];
            String folderName = filename.substring(0, filename.indexOf('.'));
            String clientName = input[1];
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
            if (owner.equals(clientName))
            {
                authorized = true;
            }
            while (metaDataStream.hasNextLine())
            {
                String current = metaDataStream.nextLine();
                String[] entries = current.split(",");
                if (entries[0].equals(clientName))
                {
                    if (entries[1].equals("owner"))
                    {
                        Date currentTime = new Date();
                        Date permissionTime = new Date(Long.parseLong(entries[2]));
                        if (currentTime.before(permissionTime))
                        {
                            authorized = true;
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
            System.out.println(directory);
            delete(directory);
            //FileUtils.deleteDirectory(directory);

            server.Writer.write("DELETED\n");
            server.Writer.flush();
            return;

        } catch (Exception e) {
            System.out.println("Exception while checking out file");
            e.printStackTrace();
        }
    }
}
