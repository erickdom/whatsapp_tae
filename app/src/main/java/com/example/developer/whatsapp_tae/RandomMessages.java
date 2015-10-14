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
            "[%s, %s] Tu saldo: %s",
            "{%s, %s} Tienes un saldo: %s ",
            "%s, %s El balance es de : %s",
            "<<%s, %s>> Tu balance -> %s Ten un linda dia!",
            "[%s, %s] El estado de tu balance es de: %s. Ventamovil te desea un buen dia."};
    public static final String[] RANDOM_STATUS = new String[] {
            "[%s, %s] \n El estatus de tu transaccion es: %s",
            "{%s, %s} \n El resultado de tu transaccion es: %s",
            "%s, %s \n Estado de tu operacion %s",
            "<<%s, %s>> \n Respuesta a su solicitud %s"};

    public static final String[] RANDOM_TIMES = new String[] {
            "El mensaje que mandaste no sera procesado por limites de tiempo",
            "Lo sentimos pero el mensaje se recibio hace mas de 2 minutos",
            ":( Tuvimos un problema y apenas leimos tu mensaje. Por motivos de tiempos no podemos procesar tu solicitud.",
            "Algo salio mal :( Y no procesamos tu mensaje en un tiempo correcto."};

    public static final String[] RANDOM_HELPERS = new String[] {
            "Buen dia le recordamos que este es un sistema automatizado. Es decir los mensajes no son enviados por una persona",
            "Necesitas ayuda, recuerda que los formatos validos son: numero*monto*compañia ó saldo. Te recordamos que este es un servicio desatendido. Esto quiere decir que los mensajes se contestan de manera automatica.",
            "Nuestro duende esta dormido. Por lo cual no podemos contestar su pregunta. Porfavor envie un formato correcto. Formatos validos(numero*monto*compañia, saldo)",
            "Hola! al parecer tratas de preguntarle algo a una maquina, te recomendamos llamar al departamento de soporte",
            "Al parecer tienes algun problema, Yo no te puedo ayudar. Por mas real que parescan, nuestras respuestas son AUTOMATICAS. Te recomendamos llamar al departamento de soporte! Ten un buen dia."};

    public static String getStringRandom(String Type, String message, String NIP){
        Random rand = new Random();
        String messageToSend;
        if(Type.compareTo("Error") == 0){
            if(message.contains("?")){
                messageToSend = String.format(RANDOM_HELPERS[rand.nextInt(RANDOM_HELPERS.length-1)],message);
            }else{
                messageToSend = String.format(RANDOM_ERRORS[rand.nextInt(RANDOM_ERRORS.length-1)],message);
            }
        }else if(Type.compareTo("Saldo") == 0){
            messageToSend = String.format(RANDOM_BALANCES[rand.nextInt(RANDOM_BALANCES.length-1)],  message);

        }else if(Type.compareTo("Tiempo") == 0){
            messageToSend = RANDOM_TIMES[rand.nextInt(RANDOM_TIMES.length-1)];

        }else{
            if(message.contains("Inicial")){

                messageToSend = String.format(RANDOM_BALANCES[rand.nextInt(RANDOM_BALANCES.length-1)], NIP, StaticFunctions.getDate(), message);
            }else{
                messageToSend = String.format(RANDOM_STATUS[rand.nextInt(RANDOM_STATUS.length-1)], NIP, StaticFunctions.getDate(), message);

            }
        }

        return messageToSend;
    }


}
