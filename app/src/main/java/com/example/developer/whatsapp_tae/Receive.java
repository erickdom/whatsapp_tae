package com.example.developer.whatsapp_tae;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;
import org.sufficientlysecure.rootcommands.util.BrokenBusyboxException;
import org.sufficientlysecure.rootcommands.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Receive extends IntentService{

    public static final int[] RANDOM_TIMES = new int[] {2000,3000,3500,2500};
    public ContentResolver contentResolver;
    private int ListenAssignments = 0;
    private int ListenSyncContacts = 0;
    public static String TAG = "RECEIVE-SERVICE";

    public Receive() {
        super("ReceiveService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Shell shell = Shell.startRootShell();
            //noinspection InfiniteLoopStatement
            while(true) Listen(shell);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void Listen(Shell shell) throws TimeoutException, BrokenBusyboxException {

        contentResolver = getContentResolver();
        this.ListenSyncContacts++;

        Random rand = new Random();
        int time = RANDOM_TIMES[rand.nextInt(3)];
        this.ListenAssignments++;
        //each x open whatsapp thats to change status to online
        if(this.ListenAssignments == 10)
        {
            openApp(getApplicationContext(),"com.whatsapp");
            this.ListenAssignments = 0;
        }
        if(this.ListenSyncContacts % 1200  == 0)
        {
            Log.d("RECEIVE","Sincronizando mis contactitos :D");
            WContacts wContacts  = new WContacts(getApplicationContext());
            wContacts.Sync();
        }

        try {
            //Tiempo entre cada receive
            Thread.sleep(time);

            //Conection to DB
            DBHelper mydb = new DBHelper(this);

            //scan what is the last id saved
            List ids = mydb.getData();
            String lastid = ids.get(0).toString();

            String str1 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"SELECT _id, data, status, timestamp, media_wa_type, key_remote_jid FROM messages WHERE _id >"+lastid+" AND key_from_me != 1 ORDER BY _id DESC;\"";

            //Execute command to scann new messages
            SimpleCommand command = new SimpleCommand(str1);
            shell.add(command).waitForFinish();

            //Read OutputCommand & use regex to transform on arrays
            String outputCommand = command.getOutput();
            outputCommand = outputCommand.replaceAll(".whatsapp.net", ".whatsapp.net|");
            String[] rows = outputCommand.split("\\|\\r?\\n");
            Log.d("OUTPUT", outputCommand);

            //In case that the local db is empty get the last index on the db of whatsapp
            if(Objects.equals(lastid, "-1"))
            {
                String[] arrayOutput = rows[0].split("\\|");
                mydb.insertLastID(arrayOutput[0]);
                return;
            }

            for(int i = 0; i<rows.length; i++){
                Log.d("ROW","ROW----->"+rows[i]);
                String[] arrayOutput = rows[i].split("\\|");
                //If array output contain more 1 element send a message
                if(arrayOutput.length>3) {
                    String message = arrayOutput[1].toLowerCase();
                    String numero = arrayOutput[5].replaceAll("\\s+", "");

                    Log.d("RECEIVE",">>>>>"+message);
                    RequestWebService request;

                    request = new RequestWebService(this);
                    if(MilitoTime.differenceMinutes(Long.parseLong(arrayOutput[3])) <= 2 ) {
                        if (this.regex(message, "saldo")) {
                            long folio = mydb.insertTransaction(message, numero);
                            String folio_send = StaticFunctions.getFolio(getApplicationContext(), folio);

                            Log.d("FOLIO",String.valueOf(folio));

                            String jsonToSend = message + "*" + folio_send + "*99*" + (numero.substring(3, 13));
                            request.execute("sms_resume", numero, jsonToSend);

                        } else if (this.regex(message, "^(\\d{10,30})(\\*)(\\d+(\\.\\d{1,2})?)(\\*)(\\d{1,2})$")) {
                            long folio = mydb.insertTransaction(message, numero);
                            String folio_send = StaticFunctions.getFolio(getApplicationContext(), folio);

                            String jsonToSend = message + "*" + folio_send + "*99*" + (numero.substring(3, 13));
                            request.execute("sms_request_transaction", numero, jsonToSend);
                        } else {
                            if (message.toLowerCase().compareTo("marco") == 0) {
                                HiloMensajes nuevomensaje = new HiloMensajes(message.toLowerCase(), numero);
                                nuevomensaje.run();
                            } else {
                                HiloMensajes nuevomensaje = new HiloMensajes(message, numero);
                                nuevomensaje.run();
                            }

                        }
                    }else{
                        HiloMensajes nuevomensaje = new HiloMensajes("1", numero);
                        nuevomensaje.run();
                    }
                    //get the first on the array rows the first is the biggest value
                    if(i==0)
                        mydb.insertLastID(arrayOutput[0]);
                }


            }
            mydb.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            dbHelper.insertLog(StaticFunctions.throwToString(e),"Error al ejecutar comandos superuser");
            dbHelper.close();
        }
    }


    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }
    public boolean regex(String string, String patternString) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }
    public class HiloMensajes implements Runnable{

        String mje;
        String num;

        public HiloMensajes(String mje, String num){
            this.mje = mje;
            this.num = num;
        }

        @Override
        public void run() {
            try {
                Messages messages;
                if(this.mje.toLowerCase().compareTo("polo") == 0) {
                    messages = new Messages(new String[]{this.num}, "La clave es marco ;) !!!");
                }else if(this.mje.toLowerCase().compareTo("marco") == 0) {
                    messages = new Messages(new String[]{this.num}, "Polo");
                }else if(this.mje.compareTo("1") == 0){
                    messages = new Messages(new String[]{this.num}, RandomMessages.getStringRandom("Tiempo",""));

                }else{
                     messages = new Messages(new String[]{this.num}, RandomMessages.getStringRandom("Error",this.mje));
                }
                messages.sendMessage();
                messages.close();

            } catch (IOException | TimeoutException e) {
                DBHelper dbHelper = new DBHelper(getApplicationContext());
                dbHelper.insertLog(StaticFunctions.throwToString(e),"Error al Crear hilo de envio de mensajes de prueba <<" + TAG + ">>");
                dbHelper.close();
            }
        }
    }
}
