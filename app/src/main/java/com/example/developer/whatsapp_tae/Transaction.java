package com.example.developer.whatsapp_tae;

public class Transaction {
    private String message;
    private String response;
    private String detalle;
    private String numero;
    private String folio;
    private String fecha;


    /**
     * Nuevo elemento de transaccion
     * @param Message mensaje que se envio desde el usuario
     * @param Response el codigo de respuesta del WS
     * @param Detalle el detalle del WS
     * @param Numero numero que envio el mensaje
     * @param Folio folio generado en la tabla transactions
     * @param Fecha fecha y hora del movimiento
     */

    public Transaction(String Message, String Response, String Detalle, String Numero, String Folio, String Fecha) {
        this.message = Message;
        this.response = Response;
        this.detalle = Detalle;
        this.numero = Numero;
        this.folio = Folio;
        this.fecha = Fecha;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getFecha() {
        return fecha;
    }

    public String getFolio() {
        return folio;
    }

    public String getMessage() {
        return message;
    }

    public String getNumero() {
        if(numero != null && numero.length()>10) {
            return numero.substring(3, 13);
        }else {
            return numero;
        }
    }

    public String getResponse() {
        return response;
    }
}
