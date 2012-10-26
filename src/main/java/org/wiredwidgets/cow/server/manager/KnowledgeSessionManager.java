/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.GenericCommandBasedWSHumanTaskHandler;
import org.jbpm.process.workitem.wsht.MinaHTWorkItemHandler;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskClientConnector;
import org.jbpm.task.service.mina.AsyncMinaTaskClient;

/**
 *
 * @author FITZPATRICK
 */
public class KnowledgeSessionManager {
    StatefulKnowledgeSession kSession;
    MinaHTWorkItemHandler minaWorkItemHandler;
    
    public void init() {
        GenericCommandBasedWSHumanTaskHandler test = new GenericCommandBasedWSHumanTaskHandler(kSession);
        minaWorkItemHandler = new MinaHTWorkItemHandler(kSession);
        minaWorkItemHandler.setIpAddress("127.0.0.1");
        minaWorkItemHandler.setPort(9123);
        kSession.getWorkItemManager().registerWorkItemHandler("Human Task", minaWorkItemHandler);
    }
    
    public StatefulKnowledgeSession getkSession() {
        return kSession;
    }
    
    public void setkSession(StatefulKnowledgeSession kSession) {
        this.kSession = kSession;
    }
    
    public MinaHTWorkItemHandler getminaWorkItemHandler() {
        return minaWorkItemHandler;
    }
    
    public void setminaWorkItemHandler(MinaHTWorkItemHandler minaWorkItemHandler) {
        this.minaWorkItemHandler = minaWorkItemHandler;
    }
}
