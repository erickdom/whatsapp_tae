package com.example.developer.whatsapp_tae;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;
import org.sufficientlysecure.rootcommands.util.BrokenBusyboxException;
import org.sufficientlysecure.rootcommands.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Receive extends IntentService{

    public static final int[] RANDOM_TIMES = new int[] {6000,7000,8000,9000};
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
        if(this.ListenAssignments == 2)
        {
            openApp(getApplicationContext(),"com.whatsapp");
            this.ListenAssignments = 0;
        }
        if(this.ListenSyncContacts % 40  == 0 && Settings.ACTIVATE_SYNC(this))
        {
            Log.d("RECEIVE","Sincronizando mis contactitos :D");
            WContacts wContacts  = new WContacts(getApplicationContext());
            wContacts.Sync();
        }

        try {
            //Tiempo entre cada receive
            Thread.sleep(time);

            //Conection to DB
            DBHelper mydb = DBHelper.getInstance(getApplicationContext());

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
            JSONObject jsonObject = new JSONObject();
//           ArrayList<String> jsonArray = new ArrayList<>();
            JSONArray jsonArray = new JSONArray();

            StringBuilder objects = new StringBuilder();

            for(int i = 0; i<rows.length; i++){
                String[] arrayOutput = rows[i].split("\\|");
                //If array output contain more 1 element send a message
                if(arrayOutput.length>3) {
                    String message = arrayOutput[1].toLowerCase();
                    String numero = arrayOutput[5].replaceAll("\\s+", "");

                    Log.d("RECEIVE",">>>>>"+message);
                    if(MilitoTime.differenceMinutes(Long.parseLong(arrayOutput[3])) <= 2 ) {
                        if (this.regex(message, "saldo") || this.regex(message, "^(\\d{10,30})(\\*)(\\d+(\\.\\d{1,2})?)(\\*)(\\d{1,2})$")) {
                            long folio = mydb.insertTransaction(message, numero);
                            String folio_send = StaticFunctions.getFolio(getApplicationContext(), folio);

                            Log.d("FOLIO",String.valueOf(folio));
                            Log.i(TAG, StaticFunctions.timeElapsed(message, "RECEIVE"));

                            JSONObject jsonObject1 = new JSONObject();
                            try {
                                jsonObject1.put("folio_android",folio_send);
                                jsonObject1.put("message",message);
                                jsonObject1.put("send",numero.substring(3, 13));
                                jsonObject1.put("webservice",Settings.WEBSERVICE(getApplicationContext()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            objects.append(jsonObject1.toString()).append(",");
                            Log.d(TAG, objects.toString());
                            jsonArray.put(jsonObject1);
                        } else {
                            HiloMensajes nuevomensaje = new HiloMensajes(message.toLowerCase(), numero);
                            nuevomensaje.run();
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
            Log.d(TAG, jsonArray.toString());
            try {
                jsonObject.put("numeros",jsonArray);
                jsonObject.put("webservice",Settings.WEBSERVICE(getApplicationContext()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RestService restService = new RestService(getApplicationContext(),jsonObject,"request.messages");
            restService.execute();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());;
            dbHelper.insertLog(StaticFunctions.throwToString(e),"Error al ejecutar comandos superuser");
            
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
                Messages messages = null;
                if(this.mje.toLowerCase().compareTo("polo") == 0) {
                    messages = new Messages(new String[]{this.num}, "La clave es marco ;) !!!");
                }else if(this.mje.toLowerCase().compareTo("marco") == 0) {
                    messages = new Messages(new String[]{this.num}, "Polo");
                }else if(this.mje.compareTo("1") == 0){
                    messages = new Messages(new String[]{this.num}, RandomMessages.getStringRandom("Tiempo","",null));

                }else{
                    if(this.mje.contains("?")){
                        messages = new Messages(new String[]{this.num}, RandomMessages.getStringRandom("Error",this.mje,null));
                    }
                }
                if(messages != null){
                    messages.sendMessage();
                    messages.close();
                }

            } catch (IOException | TimeoutException e) {
                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                dbHelper.insertLog(StaticFunctions.throwToString(e),"Error al Crear hilo de envio de mensajes de prueba <<" + TAG + ">>");
                
            }
        }
    }
}
