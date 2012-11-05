/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.ProcessInstance;
import org.wiredwidgets.cow.server.api.service.Variable;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class ProcessInstanceServiceImpl extends AbstractCowServiceImpl implements ProcessInstanceService {

    public static Logger log = Logger.getLogger(ProcessInstanceServiceImpl.class);
    private static TypeDescriptor JBPM_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.drools.runtime.process.ProcessInstance.class));
    //private static TypeDescriptor JBPM_HISTORY_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryProcessInstance.class));
    private static TypeDescriptor COW_PROCESS_INSTANCE_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessInstance.class));

    @Override
    public String executeProcess(ProcessInstance instance) {
        /*
         * //OLD STUFF org.drools.runtime.process.ProcessInstance pi =
         * kSession.startProcess(instance.getProcessDefinitionKey()); return pi.getProcessId();
         */

        Map<String, Object> vars = new HashMap<String, Object>();
        
        //content.put("content", new HashMap<String,Object>());
        if (instance.getVariables() != null) {
            for (Variable variable : instance.getVariables().getVariables()) {
                vars.put(variable.getName(), variable.getValue());
            }
        }
        // COW-65 save history for all variables
        // org.jbpm.api.ProcessInstance pi = executionService.startProcessInstanceByKey(instance.getProcessDefinitionKey(), vars);
        
        
        org.drools.runtime.process.ProcessInstance pi = kSession.startProcess(instance.getProcessDefinitionKey(), vars);
        instance.setId(Long.toString(pi.getId()));
        /*
         * //create the process name as a history-tracked variable if
         * (instance.getName() != null) { //
         * executionService.createVariable(pi.getId(), "_name",
         * instance.getName(), true); vars.put("_name", instance.getName()); }          *
         * setVariables(pi.getId(), vars); // COW-65
         *
         * // add the instance id as a variable so it can be passed to a
         * subprocess // executionService.createVariable(pi.getId(), "_id",
         * pi.getId(), false);
         *
         * if (instance.getPriority() != null) {
         * updateProcessInstancePriority(instance.getPriority().intValue(), pi);
         * }
         */
        return Long.toString(pi.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        org.drools.runtime.process.ProcessInstance pi = kSession.getProcessInstance(Long.valueOf(processInstanceId));
        Map<String,Object> var = pi.getProcess().getMetaData();
        return (pi == null) ? null : converter.convert(pi, ProcessInstance.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findAllProcessInstances() {
        return this.getCOWProcessInstances();
    }

    @Override
    public boolean deleteProcessInstance(String id) {
        try {
            kSession.abortProcessInstance(Long.parseLong(id));
            return true;
        } catch (IllegalArgumentException e) {
            log.error(e);
        }
        return false;
    }

    @Override
    public void deleteProcessInstancesByKey(String key) {
        List<ProcessInstance> procList = getCOWProcessInstances();

        for (ProcessInstance proc : procList) {
            if (proc.getKey() != null && proc.getKey().equals(key)) {
                try {
                    kSession.abortProcessInstance(Long.parseLong(proc.getId()));
                } catch (IllegalArgumentException e) {
                    log.error(e);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProcessInstance> findProcessInstancesByKey(String key) {
        List<ProcessInstance> procList = getCOWProcessInstances();

        for (ProcessInstance proc : procList) {
            if (proc.getKey() != null && !proc.getKey().equals(key)) {
                procList.remove(proc);
            }
        }

        return procList;
    }

    @Override
    public boolean updateProcessInstance(ProcessInstance instance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Process getProcessInstanceStatus(String processInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProcessInstance> findAllHistoryProcessInstances() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ProcessInstance> findHistoryProcessInstances(String key, Date endedAfter, boolean ended) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Process getV2Process(String processInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<ProcessInstance> convertProcessInstances(List<org.drools.runtime.process.ProcessInstance> source) {
        return (List<ProcessInstance>) converter.convert(source, JBPM_PROCESS_INSTANCE_LIST, COW_PROCESS_INSTANCE_LIST);
    }

    private List<ProcessInstance> getCOWProcessInstances() {
        Collection<org.drools.runtime.process.ProcessInstance> processColl = kSession.getProcessInstances();
        List processList;
        if (processColl instanceof List) {
            processList = (List) processColl;
        } else {
            processList = new ArrayList(processColl);
        }

        //Collections.sort(processList);
        return this.convertProcessInstances(processList);

    }
}
