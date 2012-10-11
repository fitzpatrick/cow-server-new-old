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

package org.wiredwidgets.cow.server.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.service.HistoryActivity;
import org.wiredwidgets.cow.server.api.service.HistoryTask;
import org.wiredwidgets.cow.server.api.service.Participation;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 *
 * @author JKRANES
 */
public interface TaskService {

    /**
     * Find all tasks for the specified user.  The returned tasks
     * will include both tasks for which the user is the assignee, and tasks
     * for which the user is a candidate.
     * @param assignee
     * @return list of tasks
     */
    List<Task> findPersonalTasks(String assignee);

    List<Task> findAllTasks();

    Task getTask(String id);

    HistoryTask getHistoryTask(String id);

    void completeTask(String id, String outcome, Map<String, String> variables);

    List<Task> findAllUnassignedTasks();

    List<Task> findGroupTasks(String user);

    void takeTask(String taskId, String userId);

    List<Task> findAllTasksByProcessInstance(String id);

    List<Task> findAllTasksByProcessKey(String id);

    /**
     * Create an ad-hoc task, i.e. one not associated with any process
     * @param task the task
     * @return the task Id
     */
    String createAdHocTask(Task task);

    void addTaskParticipatingGroup(String taskId, String groupId, String type);

    void addTaskParticipatingUser(String taskId, String userId, String type);

    List<Participation> getTaskParticipations(String taskId);

    void removeTaskParticipatingGroup(String taskId, String groupId, String type);

    void removeTaskParticipatingUser(String taskId, String userId, String type);

    void removeTaskAssignment(String taskId);

    /**
     * Retrieve historical (completed) tasks by completion date range
     * @param assignee
     * @param startDate
     * @param endDate
     * @return
     */
    List<HistoryTask> getHistoryTasks(String assignee, Date startDate, Date endDate);

    void updateTask(Task task);

    List<HistoryTask> getHistoryTasks(String processId);
    
    List<HistoryActivity> getHistoryActivities(String processInstanceId);

    /**
     * 'Orphaned' tasks include: 
     * (a) Tasks assigned to non existent users, and
     * (b) Tasks attached to candidate-groups where the group(s) have no members.
     * @return list of orphaned tasks
     */
    List<Task> findOrphanedTasks();

    Activity getWorkflowActivity(String processInstanceId, String key);

}
