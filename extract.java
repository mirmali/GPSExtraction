/*
 * extract.java
 *
 * Created on May 2, 2008, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



/**
 *
 * @author ali
 */
public class extract {

    /** Creates a new instance of extract */
    public extract() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
           java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new extract_GPS_Data().setVisible(true);
            }
        });
    }

}
