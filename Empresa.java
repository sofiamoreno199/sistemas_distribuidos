/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Empresa;

import Interfaces.IEmpresa;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author juans
 */
public class Empresa extends UnicastRemoteObject implements IEmpresa, Serializable {

    private static File arch = null;
    private static String name;
    private static String HOST;
    private static String myIP;
    private static Registry reg;
    private List<Float> asociados = new ArrayList<Float>();

    private Empresa(int i) throws RemoteException {
        asociados = new ArrayList<>();
//       System.setProperty("java.rmi.server.hostname","192.168.0.12");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, RemoteException {
        JFileChooser x = new JFileChooser();
        int r = x.showOpenDialog(null);
        File arch = x.getSelectedFile();
        Empresa e = new Empresa(0);
        if (r == JFileChooser.APPROVE_OPTION) {
            e.empezar(arch);
        } else if (r == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(null, "Operacion cancelada", "Cancelado", JOptionPane.CANCEL_OPTION);
        }
    }

    public void empezar(File arch) throws RemoteException {
        Registry myreg = LocateRegistry.getRegistry(9999);
        try {
            BufferedReader inF = new BufferedReader(new InputStreamReader(new FileInputStream(arch)));
            String lec;
            myIP = inF.readLine();
            HOST = inF.readLine();
            name = inF.readLine();
            myreg.rebind(name, this);
            reg = LocateRegistry.getRegistry(HOST, 9999);
            lec = inF.readLine();

            do {
                enviarOferta(lec);
                lec = inF.readLine();
            } while (!lec.equalsIgnoreCase("#fin"));
        } catch (IOException ex) {
            Logger.getLogger(Empresa.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Empresa.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enviarOferta(String lec) throws InterruptedException {
        System.out.println("T:" + LocalTime.now());
        String t[] = lec.split(",");
        Oferta o = new Oferta(t[0], t[1], t[2], t[3], t[4], HOST, myIP, name);
        Thread.sleep(Long.parseLong(t[5]));

    }

    @Override
    public synchronized void enviarResultado(List<Float> documentos) throws RemoteException {
        float id = documentos.get(0);
        documentos.remove(documentos.get(0));
        if (documentos.size() > 0) {
            System.out.println("Para oferta con ID " + id + " tiene entrevistados nuevos: ");
            for (Float a : documentos) {
                System.out.print(a + ",");
            }
        }

    }
}
