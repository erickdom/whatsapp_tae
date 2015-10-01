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
    public static final String urlString = Settings.URL;
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
        SoapObject request = new SoapObject(NAMESPACE, METHOD);
        if(METHOD.compareTo("sms_check_transaction") == 0 || METHOD.compareTo("whatsapp_device") == 0 )
            request.addProperty("jrquest", this.__jsonToSend);
        else {
            request.addProperty("sgateway", this.__jsonToSend);
        }


        SoapSerializationEnvelope envelope = getSoapSerializationEnvelope(request);
        HttpTransportSE transportSE = new HttpTransportSE(urlString);
        transportSE.debug = true;
        transportSE.setXmlVersionTag(Settings.XML_VERSSION);

        try {
            transportSE.call(NAMESPACE + METHOD, envelope);
            testHttpResponse(transportSE);
        } catch (IOException | XmlPullParserException e) {
//            try {
////                Log.d(TAG,"1"+this.numero);
////                Messages messages = new Messages(new String[] {this.numero}, "Hubo un problema con la petición. Intenta más tarde");
////                messages.sendMessage();
////                messages.close();
//            } catch (IOException | TimeoutException e1) {
//                e1.printStackTrace();
//            }
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
        try {
            Log.d(TAG,result);
            JSONObject jsonObject = new JSONObject(result);
            if(jsonObject.has("Confirmation")) {
                String Confirmation = jsonObject.getString("Confirmation");
                String msgResponse = jsonObject.getString("msgResponse");
                String[] arrayParse = this.__jsonToSend.split("\\*");

                DBHelper dbHelper = new DBHelper(this.context);
                String folio;
                if(arrayParse.length == 6){
                    folio = arrayParse[3];
                    dbHelper.close();
                }else{
                    folio = arrayParse[1];
                }

                String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");
                Log.d("FOLIOREQUEST",folioToUpdate);
                dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Response"), msgResponse,"1");
                dbHelper.close();

                if(Confirmation.compareTo("00") == 0) {
                    Log.d(TAG, "Confirm-->" + this.numero + "Message--->" + msgResponse);
//                    Messages message = new Messages(new String[]{this.numero}, RandomMessages.getStringRandom("Saldo",msgResponse));
//                    message.sendMessage();
//                    message.close();
                }else if(Confirmation.compareTo("24") == 0)  {
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
                    request.execute("sms_check_transaction", this.numero, recursiveJsonTosend);

                } else {
//                    Messages message = new Messages(new String[]{this.numero}, RandomMessages.getStringRandom("Status",msgResponse));
//                    message.sendMessage();
//                    message.close();
                }
            }else if(jsonObject.has("Response")){
                /**
                 *  "{'Folio_Pos':'"+FolioPos+"','User':'"+SimCliente+"'}";
                 *  string wsResp = ws.sms_check_transaction(jsonWs);
                 *  -------------------------------------------------
                 *  {"User":"6144135400","Response":"00","Description":"TRANSACCION EXITOSA",
                 *  "MSG_Response":"TRANSACCION EXITOSA TELEFONO: 5554555555 PRODUCTO: TELCEL 20.0000 FOLIO: 769223",
                 *  "Folio_Carrier":"769223", "transaction_date":"08/10/2014 04:17:00 a.m."}
                 *  ******************************************************
                 *  {"Confirmation":"12","Description":"TELEFONO NO REGISTRADO","Folio":"",
                 *  "Notice":"","transaction_date":""}
                 *  {"User":"6144135400",
                 *  ******************************************************
                 *  "Response":"07",
                 *  "Description":"RECHAZO TABLA DE TRANSACCIONES LLENA",
                 *  "MSG_Response":"RECHAZO TABLA DE TRANSACCIONES LLENA TELEFONO: 6566082326 PRODUCTO: TELCEL 30.0000 FOLIO: 207109",
                 *  "Folio_Carrier":"207109",
                 *  "transaction_date":"11/06/2015 06:08:20 a.m."}
                 *  print "Revisa transaccion"
                 */
                if(jsonObject.has("whatsapp_device")) {
                    WContacts wContacts = new WContacts(context);
                    try {
                        wContacts.insertContacts(jsonObject);
                    } catch (RemoteException | OperationApplicationException e) {
                        e.printStackTrace();
                    }

                }else{
                    if(jsonObject.getString("Response").compareTo("24") == 0)
                    {
                        RequestWebService request = new RequestWebService(this.context);
                        request.execute("sms_check_transaction", this.numero, this.__jsonToSend);
                    }else {
                        String msgResponse = jsonObject.getString("MSG_Response");
                        JSONObject jsonObjectSended = new JSONObject(this.__jsonToSend);
                        String folio = jsonObjectSended.getString("Folio_Pos");

                        DBHelper dbHelper = new DBHelper(this.context);

                        String folioToUpdate = folio.replace(Settings.APP_ID(this.context),"").replaceFirst("^0+(?!$)","");
                        Log.d("FOLIOREQUEST",folioToUpdate);
                        dbHelper.updateTransaction(folioToUpdate, jsonObject.getString("Response"), msgResponse,"1");
                        dbHelper.close();

//                        Messages messages = new Messages(new String[]{this.numero}, RandomMessages.getStringRandom("Status", msgResponse));
//                        messages.sendMessage();
//                        messages.close();
                    }
                }
            }else{
                Log.d(TAG,"2"+this.numero);

                Messages messages = new Messages(new String[] {this.numero}, "Hubo un problema con la respuesta del servidor");
                messages.sendMessage();
                messages.close();
            }

        } catch (IOException | JSONException | TimeoutException | InterruptedException e) {
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
