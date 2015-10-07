package com.example.developer.whatsapp_tae;

import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RequestWebService extends AsyncTask<String, String, String> {

    private static final boolean DEBUG_SOAP_REQUEST_RESPONSE = false;
    private static final String TAG = "RequestWebService";
    private String numero = "";
    private String __jsonToSend = "";
    public static final String NAMESPACE = Settings.NAMESPACE;
    public ContentResolver contentResolver;
    private Context context;



    public RequestWebService(Context context){
        this.context =  context;
        this.contentResolver =  context.getContentResolver();
    }

    @Override
    protected String doInBackground(String... params) {

        String resultJSON = "";
        String METHOD = params[0];
        this.numero = params[1];
        this.__jsonToSend = params[2];
        Log.d(TAG,this.__jsonToSend);
        Log.d("FOLIO",String.valueOf("20"));

        SoapObject request = new SoapObject(NAMESPACE, METHOD);
        if(METHOD.compareTo("sms_check_transaction") == 0 || METHOD.compareTo("whatsapp_device") == 0 )
            request.addProperty("jrquest", this.__jsonToSend);
        else {
            request.addProperty("sgateway", this.__jsonToSend);
        }

        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);
        HttpTransportSE transportSE = new HttpTransportSE(Settings.URL(this.context));
        transportSE.debug = true;
        transportSE.setXmlVersionTag(Settings.XML_VERSSION);

        try {
            transportSE.call(NAMESPACE + METHOD, envelope);
            testHttpResponse(transportSE);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        if(envelope.bodyIn != null) if (((SoapObject) envelope.bodyIn).getPropertyCount() > 0) {
            SoapObject resultSOAP = (SoapObject) envelope.bodyIn;
            resultJSON = resultSOAP.getProperty(0).toString();
        }
        return resultJSON;
    }

    private void testHttpResponse(HttpTransportSE ht) {
        ht.debug = DEBUG_SOAP_REQUEST_RESPONSE;
        if (DEBUG_SOAP_REQUEST_RESPONSE) {
            Log.v("SOAP RETURN", "Request XML:\n" + ht.requestDump);
            Log.v("SOAP RETURN", "\n\n\nResponse XML:\n" + ht.responseDump);
        }
    }

    private SoapSerializationEnvelope getSoapSerializationEnvelope(SoapObject request) {
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        return envelope;
    }
    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
        // In this example it is the return value from the web service
        DBHelper dbHelper = new DBHelper(this.context);

        try {
            Log.d(TAG,result);
            JSONObject jsonObject = new JSONObject(result);

            if(jsonObject.has("Confirmation")) {
                String Confirmation = jsonObject.getString("Confirmation");
                String msgResponse = jsonObject.getString("msgResponse");
                String[] arrayParse = this.__jsonToSend.split("\\*");

                String folio;
                if(arrayParse.length == 6){
                    folio = arrayParse[3];
                }else{
                    folio = arrayParse[1];
                }
                Log.d("FOLIO",folio);

                if(Confirmation.compareTo("00") == 0) {
                    Log.d(TAG, "Confirm-->" + this.numero + "Message--->" + msgResponse);
                    String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");
                    Log.d("FOLIOREQUEST", folioToUpdate);

                    dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Confirmation"), RandomMessages.getStringRandom("Status", msgResponse),"1");
                }else if(Confirmation.compareTo("24") == 0 || Confirmation.compareTo("17") == 0)  {
                    Thread.sleep(3000);
                    RequestWebService request = new RequestWebService(this.context);
                    /**
                     * saldo*folio*99
                     * 0 6563942495*20.00*22*52003*99*6561082873
                     * 1 tel para recargar
                     * 2 monto
                     * 3 carrier
                     * 4 folio
                     * 5 puerto
                     * 6 send user
                     */
                    String send = arrayParse[5];
                    String recursiveJsonTosend;
                    recursiveJsonTosend = "{\"Folio_Pos\":\""+ folio +"\",\"User\":\""+ send +"\"}";
//                    recursiveJsonTosend = "{\"Folio_Pos\":\"8990000136\",\"User\":\""+ send +"\"}";

                    String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");

                    double difference = Double.parseDouble(dbHelper.getDiffDateTransaction(folioToUpdate));
                    Log.d(TAG, String.valueOf(difference));
                    if(difference < 120.0){
                        request.execute("sms_check_transaction", this.numero, recursiveJsonTosend);
                    }else{
                        dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Response"),RandomMessages.getStringRandom("Status", msgResponse),"1");
                    }


                } else {
                    String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");
                    Log.d("FOLIOREQUEST", folioToUpdate);
                    dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Confirmation"), RandomMessages.getStringRandom("Status", msgResponse), "1");
                }
            }else if(jsonObject.has("Response")){
                if(jsonObject.has("whatsapp_device")) {
                    WContacts wContacts = new WContacts(context);
                    try {
                        wContacts.insertContacts(jsonObject);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }

                }else{
                    if(jsonObject.getString("Response").compareTo("24") == 0 || jsonObject.getString("Response").compareTo("17") == 0 )
                    {
//                        JSONObject jsonObjectSended = new JSONObject(this.__jsonToSend);
                        Thread.sleep(3000);

                        String msgResponse = jsonObject.getString("MSG_Response");

                        String[] arrayParse = this.__jsonToSend.split("\\*");

                        JSONObject jsonObjectSended = new JSONObject(this.__jsonToSend);
                        String folio = jsonObjectSended.getString("Folio_Pos");

/*                        if(arrayParse.length == 6){
                            folio = arrayParse[3];
                            dbHelper.close();
                        }else{
                            folio = arrayParse[1];
                        }*/

                        String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");

                        double difference = Double.parseDouble(dbHelper.getDiffDateTransaction(folioToUpdate));
                        Log.d(TAG, String.valueOf(difference));
                        if(difference < 120.0){
                            RequestWebService request = new RequestWebService(this.context);
                            request.execute("sms_check_transaction", this.numero, this.__jsonToSend);
                        }else{
                            dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Response"), RandomMessages.getStringRandom("Status", msgResponse), "1");
                        }

                    }else {
                        String msgResponse = jsonObject.getString("MSG_Response");
//                        JSONObject jsonObjectSended = new JSONObject(msgResponse);
                        String folio;
                        if(jsonObject.has("Folio_POS")){

                            folio = jsonObject.getString("Folio_POS");
                        }else{
                            JSONObject jsonSended = new JSONObject(this.__jsonToSend);
                            folio = jsonSended.getString("Folio_Pos");
                        }

                        String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");
                        Log.d("FOLIOREQUEST",folioToUpdate);
                        Log.d("FOLIO",folioToUpdate);

                        dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Response"),RandomMessages.getStringRandom("Status", msgResponse),"1");
                    }

                }
                dbHelper.close();

            }else{
                Messages messages = new Messages(new String[] {this.numero}, "Hubo un problema con la respuesta del servidor");
                messages.sendMessage();
                messages.close();
            }

        } catch (IOException | JSONException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            try {
                if(this.numero!=null) {
                    Messages messages = new Messages(new String[]{this.numero}, "El Servidor TAE NO RESPONDE, VUELVE A INTENTARLO");
                    messages.sendMessage();
                    messages.close();
                }
            } catch (IOException | TimeoutException e1) {
                e1.printStackTrace();
            }
    }
    }
}
