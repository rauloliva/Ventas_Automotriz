package com.automotriz.Datos;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import com.automotriz.Presentacion.ReadProperties;
import com.automotriz.logger.Logger;
import com.automotriz.Constantes.Constants;

public class Mail implements Runnable {

    private String[] destinatarios;
    private String asunto;
    private String mensaje;
    private String fileName;
    private DataHandler dataHandler;

    /**
     *
     * @param destinatarios
     * @param asunto
     * @param mensaje
     */
    public Mail(String destinatarios, String asunto, String mensaje) {
        this.destinatarios = destinatarios.split(",");
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    /**
     *
     * @param fileName
     * @param filePath
     */
    public void attachFiles(String fileName, String filePath) {
        this.fileName = fileName;
        dataHandler = new DataHandler(new FileDataSource(filePath));
    }

    /**
     * starts the thread for sending the mail
     */
    public void send() {
        this.start();
    }

    private void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        Session session = Session.getDefaultInstance(getSMTPProps());
        session.setDebug(false);
        BodyPart texto = new MimeBodyPart();
        try {
            for (String address : destinatarios) {
                texto.setText(mensaje);
                MimeMultipart multiParte = new MimeMultipart();
                //if dataHandler is ready to attach a file
                if (dataHandler != null) {
                    BodyPart adjunto = new MimeBodyPart();
                    adjunto.setDataHandler(dataHandler);
                    adjunto.setFileName(fileName);
                    multiParte.addBodyPart(adjunto);
                }

                multiParte.addBodyPart(texto);

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(Constants.CORREO_PRINCIPAL));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
                message.setSubject(asunto);
                message.setContent(multiParte);
                Transport t = session.getTransport("smtp");
                t.connect(Constants.CORREO_PRINCIPAL, Constants.PWD_PRINCIPAL);
                t.sendMessage(message, message.getAllRecipients());
                t.close();
                Logger.log("Mail to " + address + " has been sent");
                success(address);
            }
        } catch (Exception e) {
            Logger.error(e.getMessage());
            Logger.error(e.getStackTrace());
            failure();
        }
    }

    private Properties getSMTPProps() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", ReadProperties.props.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", ReadProperties.props.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", ReadProperties.props.getProperty("mail.smtp.host"));
        props.put("mail.smtp.ssl.trust", ReadProperties.props.getProperty("mail.smtp.ssl.trust"));
        props.put("mail.smtp.port", ReadProperties.props.getProperty("mail.smtp.port"));
        return props;
    }

    /**
     * Show a message of success with the email that were sent
     *
     * @param emailDest
     */
    private void success(String emailDest) {
        JOptionPane.showMessageDialog(null,
                ReadProperties.props.getProperty("mail.msg.success").replace("*", emailDest),
                ReadProperties.props.getProperty("mail.msg.success.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void failure() {
        JOptionPane.showMessageDialog(null,
                ReadProperties.props.getProperty("mail.msg.failure"),
                ReadProperties.props.getProperty("mail.msg.failure.title"),
                JOptionPane.ERROR_MESSAGE);
    }
}
