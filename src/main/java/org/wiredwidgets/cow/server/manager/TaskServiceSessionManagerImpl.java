/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
public class TaskServiceSessionManagerImpl implements TaskServiceSessionManager {
    TaskService jbpmTaskService;
    TaskServiceSession jbpmTaskServiceSession;
    
    @Override
    public void init() {
        jbpmTaskServiceSession = jbpmTaskService.createSession();
        addUserGroupToSession();
    }
    
    @Override
    public TaskService getjbpmTaskService() {
        return jbpmTaskService;
    }
    
    @Override
    public void setjbpmTaskService(TaskService jbpmTaskService) {
        this.jbpmTaskService = jbpmTaskService;
    }
    
    private void addUserGroupToSession(){
        System.out.println("TRYING TO SAVE USER START");
        for (String userName : getDefaultUsers()) {
            jbpmTaskServiceSession.addUser(new User(userName));
        }
        System.out.println("TRYING TO SAVE USER END");
    }
    
    private String[] getDefaultUsers() {
        return new String[]{"shawn", "Administrator"};
    }
    
}
