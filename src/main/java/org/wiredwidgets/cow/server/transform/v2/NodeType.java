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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.transform.v2;

/**
 * Logical node types independent of notation system
 * @author JKRANES
 */
public enum NodeType {

    START, END, TASK, DECISION_TASK, SERVICE_TASK, DIVERGING_PARALLEL_GATEWAY, CONVERGING_PARALLEL_GATEWAY,
    DIVERGING_EXCLUSIVE_GATEWAY, CONVERGING_EXCLUSIVE_GATEWAY,
    DIVERGING_COMPLEX_GATEWAY, CONVERGING_COMPLEX_GATEWAY, EXIT, SUBPROCESS

}
