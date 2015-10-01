package com.example.developer.whatsapp_tae;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;
import org.sufficientlysecure.rootcommands.util.Log;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Messages {

    private String[] __destinos = new String[10];
    private String __Meessage = "";
    private Shell __shell;
    private static final String TAG = "MESSAGE-CLASS" ;

    public Messages(String[] destinos, String message) throws IOException, TimeoutException {
        this.__destinos = destinos;
        this.__Meessage = URLDecoder.decode(message,"UTF-8").replaceAll("\\n","");
        this.__shell = Shell.startRootShell();

    }

    public void commandsTestOnClick(String command) {
        try {
            // start root shell
            Shell shell = Shell.startRootShell();

            // simple commands
            SimpleCommand command2 = new SimpleCommand(command);

            shell.add(command2).waitForFinish();

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }
    }

    public Boolean sendMessage() throws IOException, TimeoutException {
        //Inicia la vida loca :v

        commandsTestOnClick("ps | grep -w 'com.whatsapp' | awk '{print $2}' | xargs kill");
        for (String __destino : this.__destinos) {
            String str3;
            long l1;
            long l2;
            int k;
            String str1;
            String str2;
            Random localRandom = new Random(20L);

            str3 = __destino;
            l1 = System.currentTimeMillis();
            l2 = l1 / 1000L;
            k = localRandom.nextInt();

            str1 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"INSERT INTO messages (key_remote_jid, key_from_me, key_id, status, needs_push, data, timestamp, MEDIA_URL, media_mime_type, media_wa_type, MEDIA_SIZE, media_name , latitude, longitude, thumb_image, remote_resource, received_timestamp, send_timestamp, receipt_server_timestamp, receipt_device_timestamp, raw_data, media_hash, recipient_count, media_duration, origin)VALUES ('"
                    + str3
                    + "', 1,'"
                    + l2
                    + "-"
                    + k
                    + "', 0,0, '"
                    + this.__Meessage
                    + "',"
                    + l1
                    + ",'','', '0', 0,'', 0.0,0.0,'','',"
                    + l1
                    + ", -1, -1, -1,0 ,'',0,0,0); \"";

            str2 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"insert into chat_list (key_remote_jid) select '"
                    + str3
                    + "' where not exists (select 1 from chat_list where key_remote_jid='"
                    + str3 + "');\"";

            str3 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"update chat_list set message_table_id = (select max(messages._id) from messages) where chat_list.key_remote_jid='"
                    + str3 + "';\"";

            this.__shell.add(
                    new SimpleCommand(
                            "chmod 777 /data/data/com.whatsapp/databases/msgstore.db"))
                    .waitForFinish();
            this.__shell.add(new SimpleCommand(str1)).waitForFinish();
            this.__shell.add(new SimpleCommand(str2)).waitForFinish();
            this.__shell.add(new SimpleCommand(str3)).waitForFinish();

        }
        return true;
    }

    public void close() throws IOException {

        this.__shell.close();
    }




}
