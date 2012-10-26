/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.util.HashMap;
import java.util.Map;
import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.workitem.wsht.MinaHTWorkItemHandler;
import org.jbpm.task.service.TaskClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 *
 * @author FITZPATRICK
 */
public class AbstractCowServiceImpl {
    @Autowired
    protected KnowledgeBase kBase;
    
    @Autowired
    protected Jaxb2Marshaller marshaller;
    
    @Autowired
    protected ConversionService converter;
    
    @Autowired
    protected StatefulKnowledgeSession kSession;
    
    @Autowired
    protected TaskClient taskClient;
    
    @Autowired
    protected HashMap userGroups;
    
    @Autowired
    protected MinaHTWorkItemHandler minaWorkItemHandler;
    
    //@Autowired
    //protected org.jbpm.task.service.TaskServiceSession jbpmTaskServiceSession;
    
    /*
     * Sets variables.  If a variable is new to this execution, make it permanent.
     */
    protected void setVariables(String executionId, Map<String, String> variables) {
        /*Set<String> varNames = executionService.getVariableNames(executionId);
        for (String key : variables.keySet()) {
            if (!varNames.contains(key)) {
                executionService.createVariable(executionId, key, variables.get(key), true);
            }
            else {
                executionService.setVariable(executionId, key, variables.get(key));
            }
        }*/
    }
}
