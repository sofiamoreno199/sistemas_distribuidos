/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sistema;

import java.util.Comparator;
import javafx.util.Pair;

/**
 *
 * @author juans
 */
class candComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        Pair<Float, Float> p1=(Pair<Float, Float>) o1;
        Pair<Float, Float> p2=(Pair<Float, Float>) o2;
        
        return Math.round(p1.getValue()-p2.getValue());
    }
    
}
