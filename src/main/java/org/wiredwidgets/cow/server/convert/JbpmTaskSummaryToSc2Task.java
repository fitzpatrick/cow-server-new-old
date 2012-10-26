/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.wiredwidgets.cow.server.convert;

import java.util.Map;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import org.wiredwidgets.cow.server.api.service.Task;
import org.wiredwidgets.cow.server.api.service.Variable;
import org.wiredwidgets.cow.server.api.service.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

/**
 *
 * @author JKRANES
 */
public class JbpmToSc2Task extends AbstractConverter implements Converter<org.jbpm.task.query.TaskSummary, Task> {

    // NOTE: Autowiring does not work here!
    // @Autowired
    org.wiredwidgets.cow.server.service.TaskService cowTaskService = null;

    @Override
    public Task convert(org.jbpm.task.query.TaskSummary source) {

        Task target = new Task();

        target.setDescription(source.getDescription());

        if (source.getActualOwner() != null){
            target.setAssignee(source.getActualOwner().getId());
        }

        
        if (source.getCreatedOn() != null) {
            target.setCreateTime(this.getConverter().convert(source.getCreatedOn(), XMLGregorianCalendar.class));
        }

        if (source.getExpirationTime() != null) {
            target.setDueDate(this.getConverter().convert(source.getExpirationTime(), XMLGregorianCalendar.class));
        }
        target.setId(String.valueOf(source.getId()));
        target.setPriority(new Integer(source.getPriority()));

        // add variables
        /*Set<String> names = taskService.getVariableNames(source.getId());
        Map<String, Object> variables = taskService.getVariables(source.getId(), names);
        // remove process name var
        variables.remove("_name");
        for (String key : variables.keySet()) {
            Variable var = new Variable();
            var.setName(key);
            // Support strings only.  Other types will cause ClassCastException
            try {
                var.setValue((String) variables.get(key));
            } catch (ClassCastException e) {
                var.setValue("Variable type " + variables.get(key).getClass().getName() + " is not supported");
            }
            addVariable(target, var);
        }

        // Do this only if the task is not an ad-hoc task (as indicated by null executionId)
        if (source.getExecutionId() != null) {

            // name is the 'form' attribute in JPDL
            // this is used in the COW schema to store the display name, as distinct from the system-generated name
            target.setName(source.getFormResourceName());
            
            // activityName is the 'name' from JPDL
            target.setActivityName(source.getActivityName());            

            Execution ex = executionService.findExecutionById(source.getExecutionId());
            target.setProcessInstanceId(ex.getProcessInstance().getId());

            // outcomes
            Set<String> outcomes = taskService.getOutcomes(source.getId());
            for (String outcome : outcomes) {
                target.getOutcomes().add(outcome);
            }

            // Workaround to the fact that we cannot use autowiring here
            if (this.cowTaskService == null) {
                this.cowTaskService = (org.wiredwidgets.cow.server.service.TaskService) this.factory.getBean("taskService");
            }

            // add process level task varibles (
            String executionId = getTopLevelExecutionId(source.getExecutionId());
            org.wiredwidgets.cow.server.api.model.v2.Activity activity = cowTaskService.getWorkflowActivity(executionId, source.getActivityName());
            if (activity != null && activity instanceof org.wiredwidgets.cow.server.api.model.v2.Task) {
                org.wiredwidgets.cow.server.api.model.v2.Task cowTask = (org.wiredwidgets.cow.server.api.model.v2.Task) activity;
                if (cowTask.getVariables() != null) {
                    for (org.wiredwidgets.cow.server.api.model.v2.Variable var : cowTask.getVariables().getVariables()) {
                        Variable newVar = new Variable();
                        newVar.setName(var.getName());
                        newVar.setValue(var.getValue());
                        addVariable(target, newVar);
                    }
                }
            }
        } else {
            // for ad-hoc tasks
            target.setName(source.getName());
        }*/

        return target;
    }

    private void addVariable(Task task, Variable var) {
        if (task.getVariables() == null) {
            task.setVariables(new Variables());
        }
        task.getVariables().getVariables().add(var);
    }
    
    /*
     * Fix for COW-132
     * Tasks in parallel structures may cause sub-executions  with IDs in the form key.number.number.number
     * In this case we only want to look at the top level process execution.  
     */
    private String getTopLevelExecutionId(String executionId) {
        String[] parts = executionId.split("\\.");
        return parts[0] + "." + parts[1];
    }
}
