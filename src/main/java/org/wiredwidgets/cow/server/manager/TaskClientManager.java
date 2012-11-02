/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.drools.SystemEventListenerFactory;
import org.jbpm.task.TaskService;
import org.jbpm.task.service.SyncTaskServiceWrapper;
import org.jbpm.task.service.mina.AsyncMinaTaskClient;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;

/**
 *
 * @author FITZPATRICK
 */
public class TaskClientManager {
    TaskService taskClient;
    
    public void init(){
        //taskClient = new TaskClient(new MinaTaskClientConnector("client 1", new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        taskClient.connect("127.0.0.1", 9123);
    }
    
    public void gettaskClient(){
        
    }
    
    public void settaskClient(TaskService taskClient){
        this.taskClient = taskClient;
    }
}
