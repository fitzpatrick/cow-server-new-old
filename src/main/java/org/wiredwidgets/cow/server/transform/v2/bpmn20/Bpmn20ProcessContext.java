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

package org.wiredwidgets.cow.server.transform.v2.bpmn20;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.wiredwidgets.cow.server.transform.v2.AbstractProcessContext;
import org.omg.spec.bpmn._20100524.di.BPMNDiagram;
import org.omg.spec.bpmn._20100524.di.BPMNEdge;
import org.omg.spec.bpmn._20100524.di.BPMNPlane;
import org.omg.spec.bpmn._20100524.di.BPMNShape;
import org.omg.spec.bpmn._20100524.model.*;
import org.omg.spec.dd._20100524.dc.Bounds;
import org.omg.spec.dd._20100524.dc.Point;

import org.wiredwidgets.cow.server.api.model.v2.Process;


/**
 * Note that the JAXBElement must be TFlowElement rather than TFlowNode because a sequenceFlow is TFlowElement
 * but not a TFlowNode.
 * @author JKRANES
 */
public class Bpmn20ProcessContext extends AbstractProcessContext<JAXBElement<TFlowElement>, TProcess> {

    private static Logger log = Logger.getLogger(Bpmn20ProcessContext.class);

    private Definitions definitions;
    private BPMNPlane plane;
    private int xPosition = 100;
    private static org.omg.spec.bpmn._20100524.di.ObjectFactory diFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
    private static org.omg.spec.bpmn._20100524.model.ObjectFactory modelFactory = new org.omg.spec.bpmn._20100524.model.ObjectFactory();
    
    public Bpmn20ProcessContext(Process source, Definitions definitions, TProcess target) {
        super(source, target);
        this.definitions = definitions;
        BPMNDiagram diagram = new BPMNDiagram();
        plane = new BPMNPlane();
        plane.setBpmnElement(new QName(getTarget().getId()));
        diagram.setBPMNPlane(plane);
        definitions.getBPMNDiagrams().add(diagram);
    }

    @Override
    public void addNode(JAXBElement<TFlowElement> node) {
        getTarget().getFlowElements().add(node);

        // Create the diagram element, unless this is a sequenceFlow
        if (node.getName().getLocalPart().equals("sequenceFlow")) {
            BPMNEdge edge = new BPMNEdge();
            edge.setBpmnElement(new QName(node.getValue().getId()));
            Point waypoint1 = new Point();
            Point waypoint2 = new Point();
            // waypoints are required for schema validation
            // values are arbitrary until we have a real layout algorithm
            waypoint1.setX(50);
            waypoint1.setY(100);
            waypoint2.setX(50);
            waypoint2.setY(100);
            edge.getWaypoints().add(waypoint1);
            edge.getWaypoints().add(waypoint2);       
            plane.getDiagramElements().add(diFactory.createBPMNEdge(edge));
        }
        else {
            BPMNShape shape = new BPMNShape();
            shape.setBpmnElement(new QName(node.getValue().getId()));
            Bounds b = new Bounds();
            b.setHeight(50);
            b.setWidth(50);
            b.setX(xPosition +=100);
            b.setY(100);
            shape.setBounds(b);
            plane.getDiagramElements().add(diFactory.createBPMNShape(shape));
        }
    }
     
    protected Property addProcessVariable(String itemName, String dataType) {
        TItemDefinition itemDef = new TItemDefinition();
        String id = "_" + itemName + "Item";
        itemDef.setId(id);
        itemDef.setStructureRef(new QName(dataType));   
        definitions.getRootElements().add(modelFactory.createItemDefinition(itemDef));
        
        Property prop = new Property();
        prop.setId(itemName);
        prop.setItemSubjectRef(new QName(id));
        getTarget().getProperties().add(prop);    
        return prop;
    }    
}
