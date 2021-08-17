/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Candidato;

import Interfaces.ICandidato;
import Interfaces.ISistemaEmpleos;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juans
 */
public class Candidato extends UnicastRemoteObject implements Runnable,ICandidato,Serializable {

    private String nombre;
    private float documento;
    private String HOST;
    private String myIP;
    private Vector<Object> envio;

    public Candidato(String nombre, float documento, String nivelE, List<List<String>> expL, float salario, String HOST, String myIP) throws RemoteException {
        this.nombre = nombre;//0                    1                       2               3                   4
        envio = new Vector<>();
        envio.add(myIP);
        envio.add(documento);
        envio.add(nivelE);
        envio.add(expL);
        envio.add(salario);//expl: 0 cargo, 1 tiempo, 2 sector
        this.documento = documento;
        this.HOST = HOST;
        this.myIP = myIP;
    }

    @Override
    public void run() {
        try {
            System.setProperty("java.rmi.server.hostname","192.168.0.12");
            Registry reg = LocateRegistry.getRegistry(HOST, 9999);
            reg.rebind(String.valueOf(documento), this);
            ISistemaEmpleos se = (ISistemaEmpleos) reg.lookup("SE");
            se.registrarSolicitud(envio);
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Candidato.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public synchronized void enviarResultado(Vector<Object> respuesta) throws RemoteException {
        float IDoferta = (float) respuesta.get(0);
        String nomE = String.valueOf(respuesta.get(1));
        System.out.println("RESPUESTA" + LocalTime.now() + " |Mi entrevista (" + documento + ") tiene la id: " + IDoferta + " con la empresa " + nomE);
    }

}
