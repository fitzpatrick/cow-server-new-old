/**
 * Approved for Public Release: 10-4800. Distribution Unlimited. Copyright 2011
 * The MITRE Corporation, Licensed under the Apache License, Version 2.0 (the
 * "License");
 *
 * You may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import org.omg.spec.bpmn._20100524.model.*;
import org.wiredwidgets.cow.server.api.model.v2.Task;
import org.wiredwidgets.cow.server.transform.v2.ProcessContext;

/**
 *
 * @author JKRANES
 */
public class Bpmn20UserTaskNodeBuilder extends Bpmn20FlowNodeBuilder<TUserTask, Task> {
    
    private IoSpecification ioSpec = new IoSpecification();
    private InputSet inputSet = new InputSet();
    private OutputSet outputSet = new OutputSet();
    // private static QName SOURCE_REF_QNAME = new QName("http://www.omg.org/spec/BPMN/20100524/MODEL","sourceRef");

    public Bpmn20UserTaskNodeBuilder(ProcessContext context, Task task) {
        super(context, new TUserTask(), task);
    }

    @Override
    protected void buildInternal() {

        Task source = getActivity();
        TUserTask t = getNode();
        t.setId(getContext().generateId("_")); // JBPM ID naming convention uses underscore prefix + sequence 
             
        source.setKey(t.getName());
        t.setName(source.getName());

        // handle assignment
        addPotentialOwner(t, getOwnerName(source));
        
        t.setIoSpecification(ioSpec);     
        ioSpec.getInputSets().add(inputSet);       
        ioSpec.getOutputSets().add(outputSet);
        
        // standard JBPM inputs
        addDataInput("Content");
        addDataInput("Comment", source.getDescription());
        addDataInput("Skippable", "false");
        addDataInput("TaskName", source.getName());

    }

    @Override
    protected JAXBElement<TUserTask> createNode() {
        return factory.createUserTask(getNode());
    }
      
    private void addPotentialOwner(TUserTask t, String ownerName) {
        TFormalExpression formalExpr = new TFormalExpression();
        formalExpr.getContent().add(ownerName);

        ResourceAssignmentExpression resourceExpr = new ResourceAssignmentExpression();
        resourceExpr.setExpression(factory.createFormalExpression(formalExpr));

        TPotentialOwner owner = new TPotentialOwner();
        owner.setResourceAssignmentExpression(resourceExpr);
        t.getResourceRoles().add(factory.createPotentialOwner(owner));       
    }
    
    private String getOwnerName(Task source) {
        String ownerName = null;
        if (source.getAssignee() != null) {
            ownerName = source.getAssignee();
        } else if (source.getCandidateUsers() != null) {
            ownerName = getActivity().getCandidateUsers();
        } else if (source.getCandidateGroups() != null) {
            ownerName = source.getCandidateGroups();
        }
        return ownerName;
    }
    
    private String getInputRefName(String name) {
        return getNode().getId() + "_" + name + "Input"; // JBPM naming convention
    }
    
    protected void addDataInput(String name, String value) {     
        assignInputValue(addDataInput(name), value);
    }
       
    protected DataInput addDataInput(String name) {
        DataInput dataInput = new DataInput();
        dataInput.setId(getInputRefName(name));
        dataInput.setName(name); 
        ioSpec.getDataInputs().add(dataInput);
        inputSet.getDataInputRefs().add(factory.createInputSetDataInputRefs(dataInput));  
        return dataInput;
    }
    

    protected DataOutput addDataOutput(String name, boolean addProcessVar) {
        DataOutput dataOutput = new DataOutput();
        String id = getNode().getId() + "_" + name + "Output"; // JBPM naming convention
        dataOutput.setId(id);
        dataOutput.setName(name); 
        ioSpec.getDataOutputs().add(dataOutput);
        outputSet.getDataOutputRefs().add(factory.createOutputSetDataOutputRefs(dataOutput)); 
        
        DataOutputAssociation doa = new DataOutputAssociation();
        getNode().getDataOutputAssociations().add(doa);
        
        // This part is not at all obvious. Determined correct approach by unmarshalling sample BPMN2 into XML
        // and then examining the java objects
        
        // JAXBElement<Object> ref = new JAXBElement<Object>(SOURCE_REF_QNAME, Object.class, TDataAssociation.class, dataOutput);  
        doa.getSourceReves().add(factory.createTDataAssociationSourceRef(dataOutput));
        
        Property prop;
        if (addProcessVar) {
            prop = getContext().addProcessVariable(name, "String");
        }
        else {
            prop = new Property();
            prop.setId(name);
        }
        doa.setTargetRef(prop);
  
        return dataOutput;
    }    
    
    /**
     * Follows JBPM naming conventions
     * @param name
     * @param value 
     */
    protected void assignInputValue(DataInput dataInput, String value) {
        DataInputAssociation dia = new DataInputAssociation();
        getNode().getDataInputAssociations().add(dia);
        dia.setTargetRef(dataInput);

        Assignment assignment = new Assignment();
        
        TFormalExpression tfeFrom = new TFormalExpression();
        tfeFrom.getContent().add(value);
        assignment.setFrom(tfeFrom);
              
        TFormalExpression tfeTo = new TFormalExpression();
        tfeTo.getContent().add(dataInput.getId());
        assignment.setTo(tfeTo);
        
        dia.getAssignments().add(assignment); 
    }    
}
