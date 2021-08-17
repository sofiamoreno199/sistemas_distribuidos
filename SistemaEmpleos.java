/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sistema;

import Interfaces.ICandidato;
import Interfaces.IEmpresa;
import Interfaces.ISistemaEmpleos;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.util.Pair;

/**
 *
 * @author juans
 */
public class SistemaEmpleos extends UnicastRemoteObject implements ISistemaEmpleos, Serializable {

    private Vector<Object> candidatos;
    private List<Pair<Float, Float>> esperando;
    private Vector<Object> ofertas;
    private List<Pair<Transaccion, AtomicBoolean>> Tofertas;
    private static float TID;

    public Vector<Object> getCandidatos() {
        return candidatos;
    }

    public void setCandidatos(Vector<Object> candidatos) {
        this.candidatos = candidatos;
    }

    public Vector<Object> getOfertas() {
        return ofertas;
    }

    public void setOfertas(Vector<Object> ofertas) {
        this.ofertas = ofertas;
    }

    public static float getTID() {
        return TID;
    }

    public static void setTID(float TID) {
        SistemaEmpleos.TID = TID;
    }

    public List<Pair<Transaccion, AtomicBoolean>> getTofertas() {
        return Tofertas;
    }

    public void setTofertas(List<Pair<Transaccion, AtomicBoolean>> Tofertas) {
        this.Tofertas = Tofertas;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        LocateRegistry.createRegistry(9999);
//         System.setProperty("java.rmi.server.hostname","192.168.0.12");
        System.setProperty("sun.rmi.registry.registryFilter", "java.**;<Empresa>");
        System.setProperty("sun.rmi.registry.registryFilter", "java.**;<Candidato>");
        System.setProperty("sun.rmi.registry.registryFilter", "java.**;<Sistema>");

        Registry reg = LocateRegistry.getRegistry(9999);
        reg.rebind("SE", new SistemaEmpleos());
        System.out.println("SISTEMA DE EMPLEOS LISTO");
    }

    /**
     *
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public SistemaEmpleos() throws RemoteException {
        super();
        TID = 1;
        candidatos = new Vector<>();
        Tofertas = new ArrayList<>();
        esperando = new ArrayList<>();
    }

    @Override
    public synchronized void enviarOferta(Vector<Object> Oferta) {
        float id = nuevoID();
        Oferta.add(id);//7
        Tofertas.add(new Pair<>(new Transaccion(id, Oferta, this), new AtomicBoolean(false)));
        Tofertas.get(Tofertas.size() - 1).getKey().start();
        return;

    }

    public synchronized float nuevoID() {
        float tid = TID;
        TID++;
        return tid;
    }

    @Override
    public synchronized void registrarSolicitud(Vector<Object> Solicitud) {
        int R = 0, W = 0;
        Solicitud.add(R);//5
        Solicitud.add(W);//6
        Solicitud.add(false);
        System.out.println("Candidato " + Solicitud.get(1) + " ha ingresado al sistema.");
        candidatos.add(Solicitud);
        return;
    }

    void registrarBloqueo(float TID, float aprueba) throws InterruptedException {

        Transaccion a = this.getByID(TID);
        synchronized(a)
        {
                  a.wait();
        }
        esperando.add(new Pair<>(TID, aprueba));
    }

    public synchronized Pair<Float, Vector<Object>> verificarLectura(float doc, float TID) {

        Vector<Object> cand = this.getByDoc(doc);
        if (TID > Float.valueOf(String.valueOf(cand.get(6)))) {
            float Ds = this.getmaxWTS(doc);
            boolean a = this.getByIDCommit(Ds).get();;
            if (a) {
                return new Pair<>(Float.parseFloat("-1"), cand);
            } else {
                return new Pair<>(Ds, null);
            }
        } else {
            this.abort(TID);
        }
        return null;

    }

    public synchronized void verificarEscritura(Float doc, float TID) {
        Vector<Object> cand = this.getByDoc(doc);
        float Ds = this.getmaxRTS(doc);
        if (!(TID > Float.parseFloat(String.valueOf(cand.get(6))) && TID >= Ds)) {
            this.abort(TID);

        }
    }

    public synchronized void commit(ArrayList<Float> writeSet, float id, float Oid, String name, String ip) throws RemoteException, NotBoundException, UnknownHostException {
        int a = this.getByID2(id);
        Tofertas.get(a).getValue().set(true);
        int tam = 0;
        for (Float f : writeSet) {
            for (Object o : candidatos) {
                Vector<Object> obj = (Vector<Object>) o;
                if ((float) obj.get(1) == f && tam < Tofertas.get(a).getKey().getCanti()) {
                    this.avisarC((String) obj.get(0), (float) obj.get(1), Oid, name);
                    obj.set(7, true);
                    tam++;
                }
            }
        }
        Tofertas.get(a).getKey().setCanti(Tofertas.get(a).getKey().getCanti()-tam);
        System.out.println("Commit Server ");
        this.notify(id);
        ArrayList<Float> envio= new ArrayList<>();
        for(int i=0;i<tam;i++)
            envio.add(writeSet.get(i));
        this.avisarE(Oid, envio, ip, name);
        if (Tofertas.get(a).getKey().getCanti()>0) {
            Transaccion nueva = new Transaccion(nuevoID(), Tofertas.get(a).getKey());
            Tofertas.add(new Pair<Transaccion, AtomicBoolean>(nueva, new AtomicBoolean(false)));
            nueva.start();
        }

    }

    private synchronized void abort(float id) {
        Transaccion nueva = null;
        Pair<Transaccion, AtomicBoolean> rem = null;
        for (Pair<Transaccion, AtomicBoolean> t : Tofertas) {
            if (t.getKey().getTID() == id) {
                rem = t;
                t.getKey().interrupt();
                nueva = new Transaccion(nuevoID(), t.getKey());
                nueva.start();
                Tofertas.add(new Pair<Transaccion, AtomicBoolean>(nueva, new AtomicBoolean(false)));
                break;
            }
        }

    }

    private synchronized void notify(float id) {
        for (Pair<Float, Float> e : esperando) {
            if (e.getValue() == id) {
               Transaccion a = this.getByID(e.getKey());
                    a.notify();
                
            }
        }
    }

    private void avisarC(String ip, float documento, float oid, String name) throws RemoteException, NotBoundException, UnknownHostException {
        String ipe = InetAddress.getLocalHost().toString().split("/")[1];
        Registry reg = LocateRegistry.getRegistry(ipe, 9999);
        ICandidato se = (ICandidato) reg.lookup("" + documento);
        Vector<Object> respuesta = new Vector<>();
        respuesta.add(oid);
        respuesta.add(name);
        se.enviarResultado(respuesta);
    }

    private void avisarE(Float oid, ArrayList<Float> docs, String ip, String name) throws RemoteException, NotBoundException, UnknownHostException {
        String ipe = InetAddress.getLocalHost().toString().split("/")[1];
        Registry reg = LocateRegistry.getRegistry(ipe, 9999);
        IEmpresa se = (IEmpresa) reg.lookup(name);
        ArrayList<Float> respuesta = new ArrayList<>();
        respuesta.add(oid);
        docs.stream().forEach((f) -> {
            respuesta.add(f);
        });
        se.enviarResultado(respuesta);
    }

    private Transaccion getByID(float id) {
        for (Pair<Transaccion, AtomicBoolean> t : Tofertas) {
            if (t.getKey().getTID() == id) {
                return t.getKey();
            }
        }
        return null;
    }

    private int getByID2(float id) {
        for (int i = 0; i < Tofertas.size(); i++) {
            if (Tofertas.get(i).getKey().getTID() == id) {
                return i;
            }
        }
        return -1;
    }

    private AtomicBoolean getByIDCommit(float id) {
        for (Pair<Transaccion, AtomicBoolean> t : Tofertas) {
            if (t.getKey().getTID() == id) {
                return t.getValue();
            }
        }
        if (id == 0.0) {
            return new AtomicBoolean(true);
        }
        return null;
    }

    private Vector<Object> getByDoc(float doc) {
        for (Object o : candidatos) {
            Vector<Object> obj = (Vector<Object>) o;
            if ((float) obj.get(1) == doc) {
                return obj;
            }
        }
        return null;
    }

    private float getmaxWTS(float doc) {

        float maxW = 0;
        for (Pair<Transaccion, AtomicBoolean> t : Tofertas) {
            if (t.getKey().getByDocW(doc) && maxW < t.getKey().getTID()) {
                maxW = t.getKey().getTID();
            }
        }
        return maxW;
    }

    private float getmaxRTS(Float doc) {
        float maxW = 0;
        for (Pair<Transaccion, AtomicBoolean> t : Tofertas) {
            if (t.getKey().getByDocR(doc) && maxW < t.getKey().getTID()) {
                maxW = t.getKey().getTID();
            }
        }
        return maxW;
    }
}
