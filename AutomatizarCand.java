package Candidato;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * automatizarSubs:A partir de un archivo crea una serie de suscriptores.
 */
public class AutomatizarCand {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        List<Candidato> cands = new ArrayList<Candidato>();
        //System.setProperty("java.rmi.server.hostname","192.168.0.12");
        Registry myreg = LocateRegistry.getRegistry(9999);
        JFileChooser x = new JFileChooser();
        int r = x.showOpenDialog(null);
        File arch = x.getSelectedFile();
        if (r == JFileChooser.APPROVE_OPTION) {
            BufferedReader inF = new BufferedReader(new InputStreamReader(new FileInputStream(arch)));
            String Nombre;
            String estudios;
            String div[];
            String HOST;
            String myIP;
            float documento;
            float salario;
            myIP = inF.readLine();
            HOST = inF.readLine();
            String lec = inF.readLine();
            do {
                String info[] = lec.split(",");
                Nombre = info[0];
                documento = Float.parseFloat(info[1]);
                estudios = info[2];
                salario = Float.parseFloat(info[3]);
                lec = inF.readLine();
                List<List<String>> exp = new ArrayList<List<String>>();
                div = lec.split(";");
                for (int i = 0; i < div.length; i++) {
                    String expo[] = div[i].split(",");
                    List<String> expl = new ArrayList<String>();
                    for (int j = 0; j < expo.length; j++) {
                        expl.add(expo[j]);
                    }
                    exp.add(expl);
                }
                Candidato c = new Candidato(Nombre, documento, estudios, exp, salario, HOST, myIP);
                myreg.rebind(info[1], c);
                Thread t= new Thread(c);
                t.start();
                cands.add(c);
                lec = inF.readLine();
            } while (lec.compareToIgnoreCase("#fin") != 0);

        } else if (r == JFileChooser.CANCEL_OPTION) {
            JOptionPane.showMessageDialog(null, "Operacion cancelada", "Cancelado", JOptionPane.CANCEL_OPTION);
        }

    }

}
