/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.gui;

/**
 *
 * @author ejlchappin
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.eclipse.jetty.server.Server;

public class GUIServer extends SwingWorker<Void, Integer> {

    public SimpleGUI2 handler;
            
    public GUIServer(SimpleGUI2 handler) {
        this.handler = handler;
    }

    @Override
    protected Void doInBackground() {
        Server server = new Server(8080);
        server.setHandler(handler);

        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            Logger.getLogger(GUIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
