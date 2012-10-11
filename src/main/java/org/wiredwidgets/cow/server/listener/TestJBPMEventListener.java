/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.listener;

import org.drools.event.process.*;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author FITZPATRICK
 */
public class TestJBPMEventListener implements ProcessEventListener{
    
    @Override
    public void beforeProcessStarted(ProcessStartedEvent pse) {
        //System.out.println("WOOOOOOOOOOOOOOOOOO IT STARTED");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent pse) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent pce) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent pce) {
        System.out.println(pce.getProcessInstance().getProcessName() + " COMPLETED!");
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent pnte) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent pnte) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent pnle) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent pnle) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent pvce) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent pvce) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void getSessionProcessListener() {
        
    }

    public void setSessionProcessListener(StatefulKnowledgeSession kSession) {
        kSession.addEventListener(this);
    }
    
}
