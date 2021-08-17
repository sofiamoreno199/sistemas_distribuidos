/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sistema;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author juans
 */
class Transaccion extends Thread {

    private float TID;
    private SistemaEmpleos server;
    private Vector<Object> Oferta;
    private ArrayList<Float> readSet;
    private ArrayList<Float> writeSet;
    private int canti;

    Transaccion(float nuevoID, Vector<Object> Oferta, SistemaEmpleos s) {
        TID = nuevoID;
        server = s;
        readSet = new ArrayList<Float>();
        writeSet = new ArrayList<Float>();
        this.Oferta = Oferta;
        canti=3;
    }

    Transaccion(float nuevoID, Transaccion key) {
        TID = nuevoID;
        server = key.getServer();
        readSet = new ArrayList<Float>();
        writeSet = new ArrayList<Float>();
        this.Oferta = key.getOferta();
        this.canti=key.getCanti();
    }

    public int getCanti() {
        return canti;
    }

    public void setCanti(int canti) {
        this.canti = canti;
    }

    @Override
    public void run() {
        try {
            while(writeSet.isEmpty())
            {
               evaluarCandidato(Oferta); 
            }
            commit();
        } catch (InterruptedException | RemoteException | NotBoundException | UnknownHostException ex) {
            Logger.getLogger(Transaccion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private synchronized void evaluarCandidato(Vector<Object> Oferta) throws InterruptedException {
        List<Pair<Float, Float>> posiblesC = new ArrayList<>();
        Vector<Object> candidatosaux = server.getCandidatos();
        for (Object candidatosaux1 : candidatosaux) {
            float doc = (float) ((Vector<Object>) candidatosaux1).get(1);
            Vector<Object> candidato = this.verificarLectura(doc);
            if (!((boolean) candidato.get(7))) {
                posiblesC.add(new Pair<Float, Float>((float) candidato.get(1), this.evaluar(candidato)));
                readSet.add((float) candidato.get(1));
            }
        }
            Collections.sort(posiblesC, new candComparator());
            for (int i = 0; i < posiblesC.size(); i++) {
                Vector<Object> candidato = this.verificarLectura(posiblesC.get(i).getKey());
                if (!((boolean) candidato.get(7)) && posiblesC.get(i).getValue() >= 70) {
                    this.verificarEscritura(posiblesC.get(i).getKey());
                    writeSet.add(posiblesC.get(i).getKey());
                }
            }

    }

    public boolean getByDocW(float doc) {
        for (float o : writeSet) {
            if (o == doc) {
                return true;
            }
        }
        return false;
    }

    public boolean getByDocR(float doc) {
        for (float o : readSet) {
            if (o == doc) {
                return true;
            }
        }
        return false;
    }

    private float evaluar(Vector<Object> candidato) {
        float puntaje = 0, contEXP = 0;
        List<List<String>> expL = (List<List<String>>) candidato.get(3);
        float salaO = (float) candidato.get(4);
        for (List<String> aux : expL) {
            if (((String) Oferta.get(1)).equalsIgnoreCase(aux.get(0))) {
                contEXP += Float.parseFloat((String) aux.get(1));
            }
        }
        if (contEXP != 0) {
            puntaje += 20;
            float expO = Float.parseFloat((String) Oferta.get(2));
            String nivelE = (String) candidato.get(2);
            if (((String) Oferta.get(4)).equalsIgnoreCase("primaria")) {
                puntaje += 20;
            } else if (((String) Oferta.get(4)).equalsIgnoreCase("secundaria") && !nivelE.equalsIgnoreCase("primaria")) {

                puntaje += 20;

            } else if (((String) Oferta.get(4)).equalsIgnoreCase("tecnico") && (nivelE.equalsIgnoreCase("tecnico") || nivelE.equalsIgnoreCase("profesional") || nivelE.equalsIgnoreCase("posgrado"))) {
                puntaje += 20;
            } else if (((String) Oferta.get(4)).equalsIgnoreCase("profesional") && (nivelE.equalsIgnoreCase("profesional") || nivelE.equalsIgnoreCase("posgrado"))) {
                puntaje += 20;
            } else if (((String) Oferta.get(4)).equalsIgnoreCase("posgrado") && nivelE.equalsIgnoreCase("posgrado")) {
                puntaje += 20;
            }
            if (expO == contEXP) {
                puntaje += 20;
            } else if (expO < contEXP) {
                puntaje += (20 + (40 / 3));
            }
            if (salaO < Float.parseFloat(String.valueOf(Oferta.get(5)))) {
                puntaje += (40 / 3);
            }
        }

        return puntaje;

    }

    private synchronized Vector<Object> verificarLectura(float doc) throws InterruptedException {
        float aprueba = 0;
        Vector<Object> lectura = null;
        do {
            Pair<Float, Vector<Object>> prueba = server.verificarLectura(doc, TID);

            aprueba = prueba.getKey();
            if (aprueba == -1.0) {
                lectura = prueba.getValue();
            } else {
                System.out.println("" + TID + "BLOQUEO ");
                server.registrarBloqueo(TID, aprueba);
                break;
            }
        } while (aprueba != -1);

    return lectura;
}

private void verificarEscritura(Float key) {
        server.verificarEscritura(key, TID);
    }

    private void commit() throws RemoteException, NotBoundException, UnknownHostException {
        System.out.println(TID+": SE HIZO COMMIT"+" W: "+writeSet+" R: "+readSet);
        server.commit(writeSet, TID, (float) Oferta.get(7), (String) Oferta.get(6), (String) Oferta.get(0));
    }

    public float getTID() {
        return TID;
    }

    public void setTID(float TID) {
        this.TID = TID;
    }

    public SistemaEmpleos getServer() {
        return server;
    }

    public void setServer(SistemaEmpleos server) {
        this.server = server;
    }

    public Vector<Object> getOferta() {
        return Oferta;
    }

    public void setOferta(Vector<Object> Oferta) {
        this.Oferta = Oferta;
    }

    public ArrayList<Float> getReadSet() {
        return readSet;
    }

    public void setReadSet(ArrayList<Float> readSet) {
        this.readSet = readSet;
    }

    public ArrayList<Float> getWriteSet() {
        return writeSet;
    }

    public void setWriteSet(ArrayList<Float> writeSet) {
        this.writeSet = writeSet;
    }

}
