/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.manager;

import org.jbpm.task.service.TaskService;

/**
 *
 * @author FITZPATRICK
 */
public interface TaskServiceSessionManager {
    void init();
    TaskService getjbpmTaskService();
    void setjbpmTaskService(TaskService jbpmTaskService);
}
