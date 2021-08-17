 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;

import java.rmi.Remote;
import java.util.List;
import Empresa.Oferta;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 *
 * @author juans
 */
public interface IEmpresa extends Remote {
    
       public void enviarResultado(List<Float> documentos) throws RemoteException;
    
}
