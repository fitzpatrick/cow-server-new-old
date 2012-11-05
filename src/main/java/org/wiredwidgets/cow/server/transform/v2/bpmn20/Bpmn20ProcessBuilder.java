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

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.namespace.QName;

import org.omg.spec.bpmn._20100524.model.Definitions;
import org.omg.spec.bpmn._20100524.model.ObjectFactory;
import org.omg.spec.bpmn._20100524.model.TProcess;
import org.omg.spec.bpmn._20100524.model.TProcessType;
import org.springframework.stereotype.Component;
import org.wiredwidgets.cow.server.api.model.v2.Activity;
import org.wiredwidgets.cow.server.api.model.v2.Variable;
import org.wiredwidgets.cow.server.transform.v2.AbstractProcessBuilder;
import org.wiredwidgets.cow.server.transform.v2.Builder;


/**
 *
 * @author JKRANES
 */
@Component
public class Bpmn20ProcessBuilder extends AbstractProcessBuilder<Definitions> {
    
    private static ObjectFactory factory = new ObjectFactory();
    
    private static final String DEFINITIONS_ID = "Definitions"; // unclear whether this has a any meaning

    @Override
    public Definitions build(org.wiredwidgets.cow.server.api.model.v2.Process source) {
        
        Definitions definitions = new Definitions();
        definitions.setName(source.getName());
        definitions.setId(DEFINITIONS_ID);  
        definitions.setTargetNamespace("http://www.jboss.org/drools");
        definitions.setTypeLanguage("http://www.java.com/javaTypes");
        definitions.setExpressionLanguage("http://www.mvel.org/2.0");
               
        TProcess process = new TProcess();
        process.setProcessType(TProcessType.PRIVATE);
        process.setIsExecutable(Boolean.TRUE);
        process.getOtherAttributes().put(new QName("http://www.jboss.org/drools","packageName","tns"), "defaultPackage");
        process.setId("com.sample.evaluation");
        process.setName(source.getName());
        
        definitions.getRootElements().add(factory.createProcess(process));
        
        Bpmn20ProcessContext context = new Bpmn20ProcessContext(source, definitions, process);
        
        // Every process has a Map for ad-hoc content
        context.addProcessVariable("content", "java.util.Map");
             
        // Add any additional variables defined in the workflow
        if (source.getVariables() != null) {
            for (Variable var : source.getVariables().getVariables()) {
                context.addProcessVariable(var.getName(), "String");
            }       
        }        
        
        // TODO: initialize the process variables from values specified in the master workflow

        Builder startBuilder = new Bpmn20StartNodeBuilder(context);
        startBuilder.build(null);

        Activity activity = source.getActivity().getValue();
        Builder builder = createActivityBuilder(context, activity);

        builder.build(null);
        startBuilder.link(builder);

        Builder endBuilder = new Bpmn20EndNodeBuilder(context);
        endBuilder.build(null);

        builder.link(endBuilder);

        return definitions;
    }
    
}
