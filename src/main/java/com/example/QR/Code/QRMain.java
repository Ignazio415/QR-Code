/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.QR.Code;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;



@RestController
public class QRMain {
static Connection con = null;
static String tipodb="mariadb", host="localhost",porta ="3306", nomedb="utentiqr", user="root", pass="";  
static String utenti;

     public void ScriviMessaggio(String messaggio, HttpServletResponse response) 
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
            out.println(messaggio);
         
    }
    
     
     
     public  void SQLCon(HttpServletResponse response) throws IOException{
     
         String urldb ="jdbc:"+tipodb+"://"+host+":"+porta+"/"+nomedb;
     
     try {
        con = DriverManager.getConnection(urldb,user,pass);
        
        }
    catch (SQLException e) {
            e.printStackTrace();
            ScriviMessaggio("Errore Connseione al database",response);}
     
    }
    
     public void SQLClose (HttpServletResponse response) throws IOException{
         try {
                con.close();
            } catch (SQLException e) {
                ScriviMessaggio("Impossibile chiudere la connessione al Database",response);
            }
     }
     @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
     protected void CreaQR (HttpServletRequest req,HttpServletResponse resp)
            throws ServletException, IOException {

        String text = req.getParameter("text");
        if (text == null || text.trim().isEmpty()) {
            text = "Errore QR"; // default
        }
        

        try {
            // Generazione QR
            int size = 300;
            BufferedImage qrImage = generateQr(text, size);

            // Header risposta
            resp.setContentType("image/png");

            // Scrive il QR come PNG nello stream della risposta
            ImageIO.write(qrImage, "PNG", resp.getOutputStream());

        } catch (WriterException e) {
            throw new ServletException("Errore generazione QR", e);
        }
    }
     
     private BufferedImage generateQr(String text, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int grayValue = (matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
                image.setRGB(x, y, grayValue);
            }
        }
        return image;
    }
     


    @GetMapping("/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        taskService.registerEmitter(emitter);
        return emitter;
    }
    
    
    @Autowired
    private final TaskService taskService = null;
    
    
    @PostMapping("/impostazioni")
    public void Impostazioni (HttpServletRequest request, HttpServletResponse response) throws IOException{
        String Stempo=request.getParameter("temp");
        response.setContentType("text/html");
         PrintWriter out = response.getWriter();
         long tempo=0;
         tempo= Long.parseLong(Stempo);
         taskService.startTask(tempo);
         out.println("<head><meta http-equiv='refresh' content='0; URL=/streamPage?utente="+utenti+"'></head>");
         }
    
 @GetMapping("/impostazioni")
 public void FormImpostazioni(HttpServletResponse response) throws IOException{
         ScriviMessaggio("<html>"
        + "<head><title>impostazioni QR Code</title></head>"
        + "<body><form action='/impostazioni' method='post'>"
        + "<label for='temp'>tempo</label><br>"
        + "<input type='text'id='temp' name='temp'><br><br>"
        + "<input type='submit' name='conferma' value='Conferma'>"
        + "</form></body></html>",response);
    }
   
   @GetMapping("/streamPage")
    public String streamPage(HttpServletRequest req) {
        String utente = req.getParameter("utente");
        return "<html><body>"
                + "<div id='numero'></div>"
                + "<script>"
                + "var source = new EventSource('/stream');"
                + "source.onmessage = function(event) {"
                + "document.getElementById('numero').innerHTML = `<img src='/qr?text="+utente+"-${event.data}' alt='QR'>`;"
                + "};"
                + "</script>"
                + "</body></html>";
    }
    
    @GetMapping("/registrati")
    public void Registrati (HttpServletResponse response) throws IOException{
        String registrati ="<html>"+
"<head><title>Registrazione QR Code</title></head>"+
"<body>"+
"<form action='/registrati' method='post'>"+
"<label for='user'>Username</label><br>"+
"<input type='text'id='user'  name='user'><br><br>"+
"<label for='pass'>Password</label><br>"+
"<input type='password' id='pass' name='pass'><br><br>"+
"<input type='submit' name='reg' value='Registrati'>"+
"</form></body></html>";
       ScriviMessaggio(registrati,response);
        }  
    
@PostMapping("/registrati")
    public void Registrazione (HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException{
        String registrati ="<html>"+
"<head><title>Registrazione QR Code</title></head>"+
"<body>"+
"<form action='/registrati' method='post'>"+
"<label for='user'>Username</label><br>"+
"<input type='text'id='user'  name='user'><br><br>"+
"<label for='pass'>Password</label><br>"+
"<input type='password' id='pass' name='pass'><br><br>"+
"<input type='submit' name='reg' value='Registrati'>"+
"</form></body></html>";
        String RicercaUtente ="SELECT  CASE WHEN Username = '"+request.getParameter("user")+"'  THEN 1  ELSE 0 END AS trovato FROM utenti;";
         SQLCon(response);
         PreparedStatement ps = con.prepareStatement(RicercaUtente);
         ResultSet rs = ps.executeQuery();
         if(rs.next() && rs.getInt("trovato")==1){
          ScriviMessaggio(registrati,response);
          ScriviMessaggio("<br><br><h2>Utente Già esistente</h2>",response);
         }
         else{
           String InserisciUtente = "INSERT INTO utenti (Username, Password) VALUES ('"+request.getParameter("user")+"','"+request.getParameter("pass")+"')";
           ps = con.prepareStatement(InserisciUtente);
           int righeInserite = ps.executeUpdate();
           if(righeInserite > 0){
               ScriviMessaggio("<h2>Inserimento avvenuto con successo</h2>",response);
           ScriviMessaggio("<button onclick='Home()'>Home</button>"+
 "<script>function home(){window.location.href='/accedi';}</script>",response);}
           else{
              ScriviMessaggio("<h2>Errore inserimento</h2>",response);
           ScriviMessaggio("<button onclick='Home()'>Home</button>"+
 "<script>function home(){window.location.href='/accedi';}</script>",response);}
         }
         SQLClose(response);
    }
     
   @GetMapping("/accedi")
   public void accedi (HttpServletResponse response) throws IOException{
       String accedi="<html>" +
"<head><title>Accesso QR Code</title></head>" +
"<body>" +
"<form action='/accedi' method='post'>" +
"<label for='user'>Username</label><br>" +
"<input type='text' id='user' name='user'><br><br>" +
"<label for='pass'>Password</label><br>" +
"<input type='password' id='pass' name='pass'><br><br>" +
"<input type='submit' value='Accedi'>" +
  "</form>" +
"<button onclick='Registrati()'>Registrati</button>"+
 "<script>function Registrati(){window.location.href='/registrati';}</script>" +
"</body>" +
"</html>";
      
      ScriviMessaggio(accedi,response);
   }
   
 @PostMapping("/accedi")
 public void Accesso(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException{
     String accedi="<html>" +
"<head><title>Accesso QR Code</title></head>" +
"<body>" +
"<form action='/accedi' method='post'>" +
"<label for='user'>Username</label><br>" +
"<input type='text' id='user' name='user'><br><br>" +
"<label for='pass'>Password</label><br>" +
"<input type='password' id='pass' name='pass'><br><br>" +
"<input type='submit' value='Accedi'>" +
  "</form>" +
"<button onclick='Registrati()'>Registrati</button>"+
 "<script>function Registrati(){window.location.href='/registrati';}</script>" +
"</body>" +
"</html>";
     String RicercaUtente ="SELECT  CASE WHEN Username = '"+request.getParameter("user")+"' AND Password ='"+request.getParameter("pass")+"'  THEN 1  ELSE 0 END AS trovato FROM utenti;";
         SQLCon(response);
         PreparedStatement ps = con.prepareStatement(RicercaUtente);
         ResultSet rs = ps.executeQuery();
         if(rs.next() && rs.getInt("trovato")==1){
             utenti=request.getParameter("user");
          response.sendRedirect("/impostazioni");}
         else{
          ScriviMessaggio(accedi,response);
          ScriviMessaggio("<h2>Username e Password errati</h2>",response);
         }
         
         
 }
   
     
    @GetMapping()
    public void main(HttpServletResponse response) throws IOException{
      
      response.sendRedirect("/accedi");
      
}
    
}

