/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Empresa;

import Interfaces.ISistemaEmpleos;
import Candidato.Candidato;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juans
 */
public class Oferta extends Thread {

    Vector<Object> envio;
    String HOST;
    List<Float> docs;

    public Oferta(String cargo, String nivelE, String experienciaR, String salarioO, String sector, String t4, String IP, String name) {
        //                      0                       1                   2                       3               4               5                                       6
        envio = new Vector<>();
        envio.add(IP);
        envio.add(cargo);
        envio.add(experienciaR);
        envio.add(sector);
        envio.add(nivelE);
        envio.add(Integer.parseInt(salarioO));
        envio.add(name);
        HOST = t4;
        docs = new ArrayList<>();
        this.start();
    }

    @Override
    public void run() {
        try {
            Registry reg = LocateRegistry.getRegistry(HOST, 9999);
            ISistemaEmpleos se = (ISistemaEmpleos) reg.lookup("SE");
            System.out.println("Respuesta" + LocalTime.now() + "|Enviando oferta: " + envio.get(0) + " " + envio.get(1) + " " + envio.get(2) + " " + envio.get(3) + " " + envio.get(4));
            se.enviarOferta(envio);
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Candidato.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
