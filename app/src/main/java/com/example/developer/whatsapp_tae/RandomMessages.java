package com.example.developer.whatsapp_tae;

import java.util.Random;

public class RandomMessages {

    public static final String[] RANDOM_ERRORS = new String[] {
            "Revisa el formato del mensaje (%s)",
            "Ups! Tu formato (%s) tiene un problema ",
            "No entendemos lo que tratas de decirnos, verifica (%s) el formato y vuelve a intentarlo",
            "Hay un problema en lo que tratas de pedirnos (%s)",
            "No reconocemos tu mensaje (%s), Revisalo!"};
    public static final String[] RANDOM_BALANCES = new String[] {
            "Tu saldo: %s",
            "Tienes un saldo: %s ",
            "El balance es de : %s",
            "Tu balance -> %s Ten un linda dia!",
            "El estado de tu balance es de: %s. Ventamovil te desea un buen dia."};
    public static final String[] RANDOM_STATUS = new String[] {
            "El estatus de tu transaccion es: %s",
            "El resultado de tu transaccion es: %s",
            "Estado de tu operacion %s",
            "Respuesta a su solicitud %s"};

    public static final String[] RANDOM_TIMES = new String[] {
            "El mensaje que mandaste no sera procesado por limites de tiempo",
            "Lo sentimos pero el mensaje se recibio hace mas de 2 minutos",
            ":( Tuvimos un problema y apenas leimos tu mensaje. Por motivos de tiempos no podemos procesar tu solicitud.",
            "Algo salio mal :( Y no procesamos tu mensaje en un tiempo correcto."};

    public static String getStringRandom(String Type, String message){
        Random rand = new Random();
        String messageToSend;
        if(Type.compareTo("Error") == 0){
            messageToSend = String.format(RANDOM_ERRORS[rand.nextInt(RANDOM_ERRORS.length-1)],message);
        }else if(Type.compareTo("Saldo") == 0){
            messageToSend = String.format(RANDOM_BALANCES[rand.nextInt(RANDOM_BALANCES.length-1)],message);

        }else if(Type.compareTo("Tiempo") == 0){
            messageToSend = RANDOM_TIMES[rand.nextInt(RANDOM_TIMES.length-1)];

        }else{
            messageToSend = String.format(RANDOM_STATUS[rand.nextInt(RANDOM_STATUS.length-1)],message);
        }

        return messageToSend;
    }


}
