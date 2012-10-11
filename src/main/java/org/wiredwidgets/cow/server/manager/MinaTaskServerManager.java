/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.jbpm.task.service.mina.MinaTaskServer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author FITZPATRICK
 */
public class MinaTaskServerManager {
    MinaTaskServer minaTaskServer;
    
    public void init() {
        Thread thread = new Thread(minaTaskServer);
        thread.start();
    }
    
    public void getMinaTaskServer() {
        
    }

    public void setMinaTaskServer(MinaTaskServer minaTaskServer) {
        this.minaTaskServer = minaTaskServer;
    }
}
