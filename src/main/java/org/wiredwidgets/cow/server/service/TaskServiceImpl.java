/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.XMLGregorianCalendar;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.*;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.SyncTaskServiceWrapper;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskClientHandler.GetContentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.GetTaskResponseHandler;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.mina.AsyncMinaTaskClient;
import org.jbpm.task.service.mina.MinaTaskClientConnector;
import org.jbpm.task.service.mina.MinaTaskClientHandler;
import org.jbpm.task.service.responsehandlers.*;
import org.jbpm.task.utils.ContentMarshallerContext;
import org.jbpm.task.utils.ContentMarshallerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.service.*;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
public class TaskServiceImpl extends AbstractCowServiceImpl implements TaskService {

    //private static TypeDescriptor JBPM_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.task.Participation.class));
    private static TypeDescriptor COW_PARTICIPATION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Participation.class));
    private static TypeDescriptor JBPM_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.task.query.TaskSummary.class));
    private static TypeDescriptor COW_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Task.class));
    //private static TypeDescriptor JBPM_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryTask.class));
    private static TypeDescriptor COW_HISTORY_TASK_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryTask.class));
    //private static TypeDescriptor JBPM_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.history.HistoryActivityInstance.class));
    private static TypeDescriptor COW_HISTORY_ACTIVITY_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(HistoryActivity.class));

    @Transactional(readOnly = true)
    @Override
    public List<Task> findPersonalTasks(String assignee) {
        List<TaskSummary> tasks = taskClient.getTasksAssignedAsPotentialOwner(assignee, "en-UK");
        return this.convertTasks(tasks);
    }

    @Override
    public String createAdHocTask(Task task) {
        org.jbpm.task.Task newTask = this.createOrUpdateTask(task);
        return Long.toString(newTask.getId());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public Task getTask(Long id) {
        org.jbpm.task.Task task = taskClient.getTask(id);
        return this.converter.convert(task, Task.class);
    }

    @Transactional(readOnly = true)
    @Override
    public HistoryTask getHistoryTask(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void completeTask(Long id, String assignee, String outcome, Map<String, Object> results) {
        System.out.println(assignee + " starting task with ID: " + id);
        
        taskClient.start(id, assignee);
        
        org.jbpm.task.Task task = taskClient.getTask(id);
        
        Content content = taskClient.getContent(task.getTaskData().getDocumentContentId());
        
        Object result = ContentMarshallerHelper.unmarshall("org.drools.marshalling.impl.SerializablePlaceholderResolverStrategy", content.getContent(), minaWorkItemHandler.getMarshallerContext(), null);
        Map<?,?> map = (Map<?,?>)result;
        for (Map.Entry<?,?> entry : map.entrySet()){
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        //Map<String, Object> contentObj = (Map<String, Object>)map.get("content");
       // contentObj.put("testvar", "winning");
        //results.put("content", contentObj);
        ContentData contentData = ContentMarshallerHelper.marshal(results, minaWorkItemHandler.getMarshallerContext(), null);
        
        taskClient.complete(id, assignee, contentData);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllUnassignedTasks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findGroupTasks(String user) {
        // Need to add user groups to this
        List<String> groups = (List<String>) userGroups.get(user);
        /*
         * List<Status> status = new ArrayList<Status>();
         * status.add(Status.Created); status.add(Status.InProgress);
         * status.add(Status.Ready);
         */
        //BlockingTaskSummaryResponseHandler taskSummaryResponseHandler = new BlockingTaskSummaryResponseHandler();
        List<TaskSummary> tasks = taskClient.getTasksAssignedAsPotentialOwner(user, groups, "en-UK"/*, taskSummaryResponseHandler*/);
        //List<TaskSummary> tasks = taskSummaryResponseHandler.getResults();
        return this.convertTasks(tasks);
    }

    @Override
    public void takeTask(Long taskId, String userId) {
        taskClient.claim(taskId, userId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessInstance(Long id) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findAllTasksByProcessKey(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTaskParticipatingGroup(Long taskId, String groupId, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTaskParticipatingUser(Long taskId, String userId, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Participation> getTaskParticipations(Long taskId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskParticipatingGroup(Long taskId, String groupId, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskParticipatingUser(Long taskId, String userId, String type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTaskAssignment(Long taskId) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(String assignee, Date startDate, Date endDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateTask(Task task) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryTask> getHistoryTasks(String processId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<HistoryActivity> getHistoryActivities(String processInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public List<Task> findOrphanedTasks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public Activity getWorkflowActivity(String processInstanceId, String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Date convert(XMLGregorianCalendar source) {
        return source.toGregorianCalendar().getTime();
    }

    /*
     * private List<Participation>
     * convertParticipations(List<org.jbpm.api.task.Participation> source) {
     * return (List<Participation>) converter.convert(source,
     * JBPM_PARTICIPATION_LIST, COW_PARTICIPATION_LIST); }
     */
    private List<Task> convertTasks(List<org.jbpm.task.query.TaskSummary> source) {
        return (List<Task>) converter.convert(source, JBPM_TASK_LIST, COW_TASK_LIST);
    }

    /*
     * private List<HistoryTask>
     * convertHistoryTasks(List<org.jbpm.api.history.HistoryTask> source) {
     * return (List<HistoryTask>) this.converter.convert(source,
     * JBPM_HISTORY_TASK_LIST, COW_HISTORY_TASK_LIST); }
     *
     * private List<HistoryActivity>
     * convertHistoryActivities(List<org.jbpm.api.history.HistoryActivityInstance>
     * source) { return (List<HistoryActivity>) this.converter.convert(source,
     * JBPM_HISTORY_ACTIVITY_LIST, COW_HISTORY_ACTIVITY_LIST); }
     */
    //TODO: Check if you can update a task. Can you update task by just adding a task with the same ID?
    private org.jbpm.task.Task createOrUpdateTask(Task source) {
        BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();

        org.jbpm.task.Task target;
        boolean newTask = false;
        if (source.getId() == null) {
            newTask = true;
            target = new org.jbpm.task.Task();
        } else {
            target = taskClient.getTask(Long.valueOf(source.getId())/*, getTaskResponseHandler*/);
            //target = getTaskResponseHandler.getTask();
        }
        if (target == null) {
            return null;
        }
        if (source.getAssignee() != null) {
            PeopleAssignments pa = new PeopleAssignments();
            List<OrganizationalEntity> orgEnt = new ArrayList<OrganizationalEntity>();
            org.jbpm.task.User oe = new org.jbpm.task.User();
            oe.setId(source.getAssignee());
            pa.setTaskInitiator(oe);
            orgEnt.add(oe);
            pa.setPotentialOwners(orgEnt);
            target.setPeopleAssignments(pa);

        }
        if (source.getDescription() != null) {
            List<I18NText> desc = new ArrayList<I18NText>();
            desc.add(new I18NText("en-UK", source.getDescription()));
            target.setDescriptions(desc);
        }
        if (source.getDueDate() != null) {
            Deadlines deadlines = new Deadlines();
            List<Deadline> dls = new ArrayList<Deadline>();
            Deadline dl = new Deadline();
            dl.setDate(this.convert(source.getDueDate()));
            dls.add(dl);
            deadlines.setEndDeadlines(dls);
            target.setDeadlines(deadlines);
        }
        if (source.getName() != null) {
            List<I18NText> names = new ArrayList<I18NText>();
            names.add(new I18NText("en-UK", source.getName()));
            target.setNames(names);
        }
        if (source.getPriority() != null) {
            target.setPriority(source.getPriority());
        }

        TaskData td = new TaskData();
        target.setTaskData(td);
        /*
         * if (source.getProgress() != null) {
         * target.setProgress(source.getProgress()); }
         */

        // convert variables
        /*
         * if (source.getVariables() != null &&
         * source.getVariables().getVariables().size() > 0) { Map<String,
         * Object> variables = new HashMap<String, Object>(); for (Variable
         * variable : source.getVariables().getVariables()) {
         * variables.put(variable.getName(), variable.getValue()); }
         * this.taskService.setVariables(target.getId(), variables); }
         */

       // BlockingAddTaskResponseHandler addTaskResponseHandler = new BlockingAddTaskResponseHandler();
        if (newTask) {
            taskClient.addTask(target, null/*, addTaskResponseHandler*/);
        }

       /* if (addTaskResponseHandler != null) {
            target.setId(addTaskResponseHandler.getTaskId());
        }*/

        return target;
    }
}
