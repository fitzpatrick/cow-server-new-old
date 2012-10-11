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
    
    public void init() {
        GenericCommandBasedWSHumanTaskHandler test = new GenericCommandBasedWSHumanTaskHandler(kSession);
        MinaHTWorkItemHandler mina = new MinaHTWorkItemHandler(kSession);
        mina.setIpAddress("127.0.0.1");
        mina.setPort(9123);
        kSession.getWorkItemManager().registerWorkItemHandler("Human Task", mina);
    }
    
    public StatefulKnowledgeSession getkSession() {
        return kSession;
    }
    
    public void setkSession(StatefulKnowledgeSession kSession) {
        this.kSession = kSession;
    }
}
