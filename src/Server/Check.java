package Server;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by luoyinfeng on 11/26/16.
 */
public class Check {
    public static void checkIn(PublicKey pubKey, PrivateKey privKey,Server server)
    {

        try {
            // read filename from client
            String[] input = server.reader.readLine().split(",");
            String filename = input[0];
            String clientName = input[1];
            String security_flag = input[2];
            System.out.println(filename);
            String foldername =null;
            boolean authorized = false;
            boolean rewrite_meta = false;
            Boolean delegated = false;
            if (filename.indexOf('.')>=0) {
                foldername = filename.substring(0,filename.indexOf('.'));
            }
            else {
                foldername = filename;
            }

            // creating the file on server
            String path = new File(".").getCanonicalPath();
            File directory = new File(path + File.separator +"File_system");
            //File does not exist
            if (!directory.exists())
            {
                server.Writer.write("DOES_NOT_EXIST\n");
                server.Writer.flush();
                return;
            }

            File  f = new File(path + File.separator +"File_system" +File.separator + foldername);

            if (!f.exists()) {
                System.out.println("New File. Client is the owner \n ");
                authorized = true;

                if (f.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                    return;
                }

                String newpath = path + File.separator +"File_system"+ File.separator  + foldername + File.separator + filename;
                File file_copy = new File(newpath);
                boolean bool = false;
                bool = file_copy.createNewFile();
                if(!bool)
                {
                    System.out.println("Some error occured. Could not create copy of file" + filename+"\n");
                    return;
                }
                File metadata = new File(path + File.separator+ "File_system"+ File.separator + foldername + File.separator+ "metadata.txt");
                bool = false;
                bool = metadata.createNewFile();
                if(!bool)
                {
                    System.out.println("Some error occured. Could not create of metadata file\n");
                    return;
                }
                FileWriter metaWriter = new FileWriter (metadata);
                PrintWriter pWriter = new PrintWriter (metaWriter);
                pWriter.println (server.clientname);
                pWriter.println (security_flag);
                pWriter.close();

            }

            else  {
                //rewrite_meta = false;
                File metaData = new File(path + File.separator+ "File_system"+ File.separator + foldername + File.separator+ "metadata.txt");
                Scanner metaDataStream = new Scanner(metaData);
                String owner = metaDataStream.nextLine();
                String securityFlag = metaDataStream.nextLine();
                if (owner.equals(clientName))
                {
                    rewrite_meta =true;
                    authorized = true;
                }
                else
                {
                    delegated = false;
                    while (metaDataStream.hasNextLine())
                    {
                        String current = metaDataStream.nextLine();
                        String[] entries = current.split(",");
                        if (entries[0].equals(clientName) || entries[0].equals("ALL"))
                        {

                            Date currentTime = new Date();
                            Date permissionTime = new Date(Long.parseLong(entries[2]));
                            if (currentTime.before(permissionTime))
                            {
                                delegated = true;
                                System.out.println("delegation found:" + delegated);
                                break;
                            }

                        }
                    }
                    if (!delegated) {
                        rewrite_meta = true;
                        authorized = true;
                    }
                }
                metaDataStream.close();

                if (authorized || delegated )
                {

                    //delete and create new files.
                    File f2 = new File(path + File.separator +"File_system"+ File.separator  + foldername + File.separator + filename);
                    if(f2.exists()) {
                        f2.delete();
                    }
                    System.out.println("File " + filename + " exists. Overwriting file \n ");
                    File file_copy = new File(path + File.separator +"File_system"+ File.separator  + foldername + File.separator + filename);
                    boolean bool = false;
                    bool = file_copy.createNewFile();
                    if(!bool)
                    {
                        System.out.println("Some error occured. Could not create copy of file" + filename+"\n");
                        return;
                    }
                    if (rewrite_meta && !delegated) {
                        if(metaData.exists()) {
                            System.out.println("Deleting metadata file");
                            boolean b = metaData.delete();
                            System.out.println("deleted metadata file:" + b);
                        }
                        File metadata = new File(path + File.separator+ "File_system"+ File.separator + foldername + File.separator+ "metadata.txt");
                        bool = false;
                        bool = metadata.createNewFile();
                        if(!bool)
                        {
                            System.out.println("same owner but cant create file !!Some error occured. Could not create of metadata file\n");
                            return;
                        }
                        FileWriter metaWriter = new FileWriter (metadata);
                        PrintWriter pWriter = new PrintWriter (metaWriter);
                        pWriter.println (server.clientname);
                        pWriter.println (security_flag);
                        pWriter.close();
                    }
                    //return;
                }
                else {
                    return;
                }
            }
            File  file = new File(path + File.separator+ "File_system" + File.separator +foldername + File.separator + filename);

            //read filesize from client
            String size = server.reader.readLine();
            int filesize = Integer.parseInt(size);

            byte filebytes[] = new byte[filesize];
            int bytesread = 0;

            //reading the file sent from client
            while(bytesread < filesize) {
                int thisread = server.Data_Input.read(filebytes, bytesread, filesize - bytesread);
                if(thisread >= 0) {
                    bytesread += thisread;
                } else {
                    System.out.println("Encountered an error while downloading file");
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            if (security_flag.equals("CONFIDENTIALITY")) {
                System.out.println("Document Encryption required");

                //Generating random AES key
                KeyGenerator keygen = KeyGenerator.getInstance("AES");
                keygen.init(128);
                byte[] aesKeyBytes = keygen.generateKey().getEncoded();

                //Encrypting file contents using above generate key
                SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes,"AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                byte[] cipherText = new byte[cipher.getOutputSize(filesize)];
                int ctLength = cipher.update(filebytes, 0, filesize, cipherText, 0); // filebytes contain the data of file in byte[] format
                ctLength += cipher.doFinal(cipherText, ctLength);
                System.out.println(new String(cipherText));
                System.out.println(ctLength);

                //Writing encrypted file
                bos.write(cipherText, 0, ctLength);
                bos.flush();

                //Encrypting AES key with Server's Public key
                Cipher pkCipher = Cipher.getInstance("RSA");
                pkCipher.init(Cipher.ENCRYPT_MODE, pubKey);
                byte[] encryption_key = pkCipher.doFinal(aesKeyBytes);
                byte[] orig_sign = {0x00,0x00,0x00,0x00}; // Just initializing the value
                // writing file contents into the hashmap declared - serialization to be done
                MetaData metadata = new MetaData(encryption_key,security_flag,orig_sign);

                // NEED to write this onto a metadata file.
                server.hashMap.put(filename, metadata);


                //Decryption pass to be used later
                cipher.init(Cipher.DECRYPT_MODE, aesKey);
                byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
                int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
                ptLength += cipher.doFinal(plainText, ptLength);
                System.out.println(new String(plainText));
                System.out.println(ptLength);



            } else if (security_flag.equals("INTEGRITY")) {
                System.out.println("Document Signing required");

                //Signing the document using private key of server
                Signature dsa = Signature.getInstance("SHA1withRSA");
                dsa.initSign(privKey);
                dsa.update(filebytes); // filebytes contain the data of file in byte[] format
                byte[] orig_sign = dsa.sign();
                System.out.println("Signature of the document received is " + orig_sign.toString());

                // Writing the file related values into hashmap
                byte[] encryption_key = {0x00,0x00,0x00,0x00}; // Just initializing the value
                MetaData metadata = new MetaData(encryption_key,security_flag,orig_sign);
                server.hashMap.put(filename, metadata);

                //Writing plain file
                bos.write(filebytes, 0, filesize);
                bos.flush();

            } else if (security_flag.equals("NONE")) {
                System.out.println("Security flag chosen is NONE. No manipulation required");

                byte[] encryption_key = {0x00,0x00,0x00,0x00}; // Just initializing the value
                byte[] orig_sign = {0x00,0x00,0x00,0x00};
                MetaData metadata = new MetaData(encryption_key,security_flag,orig_sign);
                server.hashMap.put(filename, metadata);

                //Writing plain file
                bos.write(filebytes, 0, filesize);
                bos.flush();
            }
            bos.close();
            fos.close();

            System.out.println("File successfully received "+ filename + "\n");

        } catch(Exception e) {
            System.out.println("Exception while checking in file");
            e.printStackTrace();
        }
    }
    public static void checkOut(PublicKey pubKey, PrivateKey privKey, Server server)
    {
        try {
            // get filename from client
            String[] input = server.reader.readLine().split(",");
            String filename = input[0];
            System.out.println(filename);
            String folderName = filename.substring(0, filename.indexOf('.'));
            String clientName = input[1];
            System.out.println(filename);


            File f;

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

                    Date currentTime = new Date();
                    Date permissionTime = new Date(Long.parseLong(entries[1]));
                    if (currentTime.before(permissionTime))
                    {
                        authorized = true;
                    }

                }
            }
            metaDataStream.close();
            System.out.println("Authorized: " + authorized);
            if (!authorized)
            {
                server.Writer.write("NOT_AUTHORIZED\n");
                server.Writer.flush();
                return;
            }
            f = new File(path + "/File_system/" + folderName + "/" + filename);


            // Read file into memory
            int filesize = (int) f.length();
            byte filebytes[] = new byte[filesize];
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(filebytes, 0, filesize);
            bis.close();
            fis.close();

            //Retrieving values from HashMap
            MetaData metadata = server.hashMap.get(filename);

            if (securityFlag.equals("CONFIDENTIALITY")) {
                //Encrypted file present - Decrypt and then send
                System.out.println("Sending encrypted file in plaintext to client");
                //Decrypting AES-encrypted key using Server's private key
                Cipher skCipher = Cipher.getInstance("RSA");
                skCipher.init(Cipher.DECRYPT_MODE, privKey);
                byte[] aesKey = skCipher.doFinal(metadata.encryption_key);

                //Decrypting the filebytes contents using above decrypted AES key
                SecretKeySpec keySpec = new SecretKeySpec(aesKey,"AES");
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] plainText = new byte[cipher.getOutputSize(filesize)];
                int ptLength = cipher.update(filebytes, 0, filesize, plainText, 0);
                ptLength += cipher.doFinal(plainText, ptLength);
                System.out.println("Decrypted contents before sending are");
                System.out.println(new String(plainText));
                System.out.println(ptLength);

                //Sending decrypted file length to the client to enable it reading
                server.Writer.write(ptLength + "\n");
                server.Writer.flush();

                //Sending decrypted file contents to client
                server.Data_Out.write(plainText, 0, ptLength);
                server.Data_Out.flush();


            } else if (securityFlag.equals("INTEGRITY")) {
                //Signed file present
                System.out.println("Sending plain file after checking its integrity");
                //Verifying signature of the file before sending
                Signature sign = Signature.getInstance("SHA1withRSA");
                sign.initVerify(pubKey);
                sign.update(filebytes, 0, filesize);
                boolean verifies = sign.verify(metadata.orig_sign);
                if (!verifies) {
                    System.out.println("Calculated signature does not match with saved sign");
                    server.Writer.write("SIGNATURE_MISMATCH" + "\n");
                    server.Writer.flush();
                    return;
                }

                //Sending file length to client
                server.Writer.write(filesize + "\n");
                server. Writer.flush();
                //Sending the file to client
                server.Data_Out.write(filebytes, 0, filesize);
                server.Data_Out.flush();
            } else if (securityFlag.equals("NONE")) {
                //Plain file present
                System.out.println("Sending plain text file to client");
                //Sending file length to client
                server.Writer.write(filesize + "\n");
                server.Writer.flush();
                //Sending the file to cient
                server.Data_Out.write(filebytes, 0, filesize);
                server.Data_Out.flush();
            }

            System.out.println("Successfully sent " + filename);
            server.Writer.write(securityFlag + "\n");
            server.Writer.flush();


        } catch (Exception e) {
            System.out.println("Exception while checking out file");
            e.printStackTrace();
        }
    }

}
