/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    HashMap userGroups;
    
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
    
    @Override
    public HashMap getuserGroups(){
        return userGroups;
    }
    
    @Override
    public void setuserGroups(HashMap userGroups){
        this.userGroups = userGroups;
    }
    
    private void addUserGroupToSession(){
        System.out.println("TRYING TO SAVE USER START");
        
        List <String> groups = getAllGroups();
        for (String group: groups){
            jbpmTaskServiceSession.addGroup(new Group(group));
        }
        
        for (String username : getDefaultUsers()) {
            jbpmTaskServiceSession.addUser(new User(username));
            
            groups = getUserGroups(username);
            for (String group: groups){
                if (userGroups.containsKey(username)){
                    ((List) userGroups.get(username)).add(group);
                } else{
                    List <String> values = new ArrayList<String>();
                    values.add(group);
                    userGroups.put(username, values);
                }
            }
            
        }
        System.out.println("TRYING TO SAVE USER END");
    }
    
    private String[] getDefaultUsers() {
        return new String[]{"shawn", "Administrator"};
    }
    
    private List<String> getUserGroups(String username){
        if (username.equals("shawn")){
            List<String> groups = new ArrayList<String>();
            groups.add("group1");
            groups.add("group2");
            return groups;
        } else if (username.equals("Administrator")){
            List<String> groups = new ArrayList<String>();
            groups.add("admin");
            return groups;
        }
        return null;
    }
    
    private List<String> getAllGroups(){
        List<String> groups = new ArrayList<String>();
        groups.add("group1");
        groups.add("group2");
        groups.add("admin");
        return groups;
    }
}
