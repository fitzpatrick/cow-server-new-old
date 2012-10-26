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

package org.wiredwidgets.cow.server.web;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.AsyncTaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wiredwidgets.cow.server.api.service.*;
import org.wiredwidgets.cow.server.service.ProcessInstanceService;
import org.wiredwidgets.cow.server.service.ProcessService;


/**
 *
 * @author FITZPATRICK
 */
@Controller
@RequestMapping("processInstances")
public class ProcessInstancesController extends CowServerController{
    private static Logger log = Logger.getLogger(ProcessInstancesController.class);
    
    @Autowired
    ProcessService processService;
    @Autowired
    ProcessInstanceService processInstanceService;
    @Autowired
    StatefulKnowledgeSession kSession;
    /**
     * Starts execution of a new process instance.  The processInstance representation
     * must contain, at minimum, a processDefinitionKey element to identify the process,
     * as well as any variables.  Note that if variables are not needed, it may be more
     * convenient to use startExecutionSimple.
     *
     * The response Location header will contain the URL of the newly created instance
     * @param pi
     * @param initVars set to 'true' to trigger initialization of variables with default values taken 
     * from the workflow Process element
     * @param response
     * @param req 
     */
    @RequestMapping(value = "/active", method = RequestMethod.POST, params = "!execute")
    public void startExecution(@RequestBody org.wiredwidgets.cow.server.api.service.ProcessInstance pi, @RequestParam(value = "init-vars", required = false) boolean initVars, HttpServletResponse response, HttpServletRequest req) {
        log.debug("startExecution: " + pi.getProcessDefinitionKey());
        
        // option to initialize the process instance with variables / values set in the master process
        /*if (initVars) {
            org.wiredwidgets.cow.server.api.model.v2.Process process = processService.getV2Process(pi.getProcessDefinitionKey());
            for (org.wiredwidgets.cow.server.api.model.v2.Variable var : process.getVariables().getVariables()) {
                addVariable(pi, var.getName(), var.getValue());
            }
        }*/
        
        String id = processInstanceService.executeProcess(pi);
        System.out.println("STARTED PROCESS ID " + id);
        response.setStatus(HttpServletResponse.SC_CREATED); // 201
        response.setHeader("Location", req.getRequestURL() + "/" + id);
    }
    
    private void addVariable(ProcessInstance pi, String name, String value) {
        if (pi.getVariables() == null) {
            pi.setVariables(new Variables());
        }
        Variable v = new Variable();
        v.setName(name);
        v.setValue(value);
        pi.getVariables().getVariables().add(v);
    }
    
    /**
     * Retrieve a specific process instance by its ID
     *
     * Current JBPM implementation assigns processInstanceId using
     * the format {processKey}.{uniqueNumber}
     * @param id the process key. If the key includes "/" character(s), the key must be doubly URL encoded.  I.e. "/" becomes "%252F"
     * @param ext the numeric extension of the process instance, or the wildcard "*" for all instances
     * @param response
     * @return a ProcessInstance object, if the extension specifies a single instance.  If the extension is the "*" wildcard,
     * then the return value will be an ProcessInstances object.  If a single ProcessInstance is requested and it does not exist,
     * a 404 response will be returned.
     */
    @RequestMapping(value = "/active/{id}.{ext}", method = RequestMethod.GET)
    @ResponseBody
    public Object getProcessInstance(@PathVariable("id") String id, @PathVariable("ext") String ext, HttpServletResponse response) {
        if (ext.equals("*")) {
            ProcessInstances pi = new ProcessInstances();
            // note: decoding is applied to the id primarily to handle possible "/" characters
            pi.getProcessInstances().addAll(processInstanceService.findProcessInstancesByKey(decode(id)));
            return pi;
        } else {
            //org.wiredwidgets.cow.server.api.service.ProcessInstance instance = processInstanceService.getProcessInstance(id + '.' + ext);
            org.wiredwidgets.cow.server.api.service.ProcessInstance instance = processInstanceService.getProcessInstance(ext);
            if (instance == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            }
            return instance;
        }
    }
    
    /**
     * Retrieve all active process instances
     * @return a ProcessInstances object as XML
     */
    @RequestMapping("/active")
    @ResponseBody
    public ProcessInstances getAllProcessInstances() {
        return createProcessInstances(processInstanceService.findAllProcessInstances());
    }
    
    /**
     * Delete a process instance, or all instances for a key
     * @param id the process key. Doubly URL encode if it contains "/"
     * @param ext the process instance number, or "*" to delete all for the key
     * @param response
     */
    @RequestMapping(value = "/active/{id}.{ext}", method = RequestMethod.DELETE)
    public void deleteProcessInstance(@PathVariable("id") String id, @PathVariable("ext") String ext, HttpServletResponse response) {
        String instanceId = "";
    	id = decode(id);
        if (ext.equals("*")) {
            processInstanceService.deleteProcessInstancesByKey(id);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
        } else {
            instanceId = id + '.' + ext;
            if (processInstanceService.deleteProcessInstance(instanceId)) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            }
        }
        
        /*if (instanceId == ""){
        	amqpNotifier.amqpProcessPublish(id, "process", "ProcessDeleted");
        } else {
        	amqpNotifier.amqpProcessPublish(instanceId, "process", "ProcessDeleted");
        }*/
        
    }
    
    private ProcessInstances createProcessInstances(List<org.wiredwidgets.cow.server.api.service.ProcessInstance> instances) {
        ProcessInstances pi = new ProcessInstances();
        pi.getProcessInstances().addAll(instances);
        return pi;
    }
    
    /**
     * Retrieve HistoryActivities for the specified process instance.  HistoryActivities include all
     * completed and pending activities for the process.  This method may be used
     * for both open and complete ProcessInstances.
     * @param id the process key.  Doubly URL encode if it contains "/".  
     * @param ext
     * @param response
     * @return a HistoryActivities object as XML
     */
    @RequestMapping("/active/{id}.{ext}/activities")
    @ResponseBody
    public HistoryActivities getProcessInstanceActivities(@PathVariable("id") String id, @PathVariable("ext") String ext, HttpServletResponse response) {
        /*HistoryActivities ha = new HistoryActivities();
        List<HistoryActivity> activities = taskService.getHistoryActivities(decode(id) + '.' + ext);
        ha.getHistoryActivities().addAll(activities);
        return ha;*/
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Returns a Process object with completion status attributes set, for a specified ProcessInstance ID. 
     * The completion status attributes are computed for each activity within the process.
     * @param id the process key.  Doubly URL encode if it contains "/".
     * @param ext
     * @param response
     * @return
     * @see org.wiredwidgets.cow.server.completion.CompletionState
     */
    @RequestMapping("/active/{id}.{ext}/status")
    @ResponseBody
    public org.wiredwidgets.cow.server.api.model.v2.Process getProcessInstanceStatus(@PathVariable("id") String id, @PathVariable("ext") String ext, HttpServletResponse response) {
        //return processInstanceService.getProcessInstanceStatus(decode(id) + "." + ext);
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Update an active process instance.  Only Priority and Variables will be updated.
     * @param pi
     * @param id the process key.  Doubly URL encode if it contains "/"
     * @param ext
     * @param response
     */
    @RequestMapping(value = "/active/{id}.{ext}", method = RequestMethod.POST)
    public void updateProcessInstance(@RequestBody org.wiredwidgets.cow.server.api.service.ProcessInstance pi, @PathVariable("id") String id, @PathVariable("ext") String ext, HttpServletResponse response) {
        // use ID of the URL
        /*pi.setId(decode(id) + "." + ext);
        if (processInstanceService.updateProcessInstance(pi)) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }*/
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Simplified variation of startExecution to execute a process with no initial variables
     * Requires no XML body content
     * @param execute the process definition key to execute
     * @param name a name to be used for this process instance.  Not strictly required to be
     * unique, but as this is often used for display to users, it should at least be unique relative
     * to other active processes.
     * @param response
     * @param req 
     */ 
    @RequestMapping(value = "/active", method = RequestMethod.POST, params = "execute")
    public void startExecutionSimple(@RequestParam("execute") String execute, @RequestParam(value="name", required=false) String name, HttpServletResponse response, HttpServletRequest req) {
        /*org.wiredwidgets.cow.server.api.service.ProcessInstance pi = new org.wiredwidgets.cow.server.api.service.ProcessInstance();
        pi.setProcessDefinitionKey(execute);
        pi.setName(name);
        startExecution(pi, false, response, req);*/
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Retrieve the history for a specified process key
     * @param key the process definition key
     * @param endedAfter YYYY-MM-DD
     * @param ended specify 'false' to include active process instances in addition to processes that have ended,
     * otherwise only completed processes will be included.
     * @return a ProcessInstances object as XML
     */
    @RequestMapping("/history")
    @ResponseBody
    public ProcessInstances getHistoryProcessInstances(@RequestParam(value = "key", required = false) String key, @RequestParam(value = "endedAfter", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endedAfter, @RequestParam(value = "ended", defaultValue = "true") boolean ended) {
        /*ProcessInstances pi = new ProcessInstances();
        pi.getProcessInstances().addAll(processInstanceService.findHistoryProcessInstances(key, endedAfter, ended));
        return pi;*/
        throw new UnsupportedOperationException("Not supported yet.");
    }  
    
    /**
     * Retrieve all processInstances that have open tasks, and include the tasks with
     * the processInstance elements.  This provides an efficient method of 
     * retrieving the processInstance attributes along with the task information, 
     * rather than making separate calls for the tasks and the processInstances.
     * @return a ProcessInstances object as XML
     */
    @RequestMapping("/tasks")
    @ResponseBody
    public ProcessInstances getProcessInstancesWithTasks() {
        
        //return createProcessInstances(mergeTasks(taskService.findAllTasks()));
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Same as above, but retrieve tasks only for the specified assignee.
     * @param assignee
     * @return 
     * @see #getProcessInstancesWithTasks() 
     * @see TasksController#getTasksByAssignee(String assignee)
     */
    @RequestMapping(value = "/tasks", params = "assignee")
    @ResponseBody
    public ProcessInstances getProcessInstancesWithTasksForAssignee(@RequestParam("assignee") String assignee) {
        //return createProcessInstances(mergeTasks(taskService.findPersonalTasks(assignee)));
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Same as above, but retrieve only unassigned tasks. 
     * @return 
     * @see #getProcessInstancesWithTasks() 
     * @see TasksController#getUnassignedTasks()
     */
    @RequestMapping(value = "/tasks", params = "unassigned=true")
    @ResponseBody
    public ProcessInstances getProcessInstancesWithUnassignedTasks() {
        //return createProcessInstances(mergeTasks(taskService.findAllUnassignedTasks()));
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Same as above, but retrieve only unassigned tasks. 
     * @return 
     * @see #getProcessInstancesWithTasks() 
     * @see TasksController#getUnassignedTaskssByCandidate(String candidate)
     */
    @RequestMapping(value = "/tasks", params = "candidate")
    @ResponseBody
    public ProcessInstances getProcessInstancesWithTasksForCandidate(@RequestParam("candidate") String candidate) {
        //return createProcessInstances(mergeTasks(taskService.findGroupTasks(candidate)));
        throw new UnsupportedOperationException("Not supported yet.");
    }  
}
