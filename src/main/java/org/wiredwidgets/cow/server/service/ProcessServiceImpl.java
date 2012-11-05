/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.omg.spec.bpmn._20100524.model.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.ResourceNames;
import org.wiredwidgets.cow.server.parse.BPMN2SaxParser;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20ProcessBuilder;
import org.wiredwidgets.rem2.schema.Node;
import org.wiredwidgets.rem2.schema.Property;
import org.xml.sax.XMLReader;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
@Component
public class ProcessServiceImpl extends AbstractCowServiceImpl implements ProcessService {

    private static Logger log = Logger.getLogger(ProcessServiceImpl.class);
    
    @Autowired
    Bpmn20ProcessBuilder bpmn20ProcessBuilder;
    
    @Autowired
    Jaxb2Marshaller marshaller;

    @Transactional(readOnly = true)
    @Override
    public StreamSource getResource(String id, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteDeployment(String id) {
        kBase.removeProcess(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Deployment> findAllDeployments() throws Exception {
        Collection<org.drools.definition.process.Process> procs = kBase.getProcesses();
        List<Deployment> deploys = new ArrayList<Deployment>();
        for (org.drools.definition.process.Process p : procs) {
            Deployment d = new Deployment();
            d.setId(p.getId());
            d.setName(p.getName());
            deploys.add(d);
        }
        return deploys;
    }

    @Transactional(readOnly = true)
    @Override
    public ResourceNames getResourceNames(String deploymentId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getNativeProcessAsStream(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public Deployment getDeployment(String id) {
        org.drools.definition.process.Process p = kBase.getProcess(id);

        Deployment d = new Deployment();
        d.setId(p.getId());
        d.setName(p.getName());

        return d;
    }

    @Override
    public Deployment createDeployment(StreamSource source, String name, boolean bpmn2) {

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            BPMN2SaxParser bpmnParser = new BPMN2SaxParser();

            byte[] input = IOUtils.toByteArray(source.getInputStream());
            ByteArrayInputStream ba = new ByteArrayInputStream(input);
            sp.parse(ba, bpmnParser);

            KnowledgeBuilder kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kBuilder.add(ResourceFactory.newByteArrayResource(input), ResourceType.BPMN2);
            kBase.addKnowledgePackages(kBuilder.getKnowledgePackages());

            return bpmnParser.getDeployment();
        } catch (Exception e) {
            log.error(e);
        }

        return null;
    }

	@Override
    public Deployment saveV2Process(Process v2Process, String deploymentName) {

       Definitions d = bpmn20ProcessBuilder.build(v2Process);
       log.debug("built bpmn20 process");
       // d.setId(v2Process.getKey());
       // resources.put(BPMN2_EXTENSION, marshalToInputStream(d));
       saveInRem2(v2Process, d);
       return createDeployment(new StreamSource(marshalToInputStream(d)), deploymentName, true);
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getResourceAsStream(String key, String extension) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public InputStream getResourceAsStreamByDeploymentId(String id, String extension) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Transactional(readOnly = true)
    @Override
    public Process getV2Process(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void addProperty(Node node, String name, String value) {
        Property p = new Property();
        p.setName(name);
        p.setValue(value);
        node.getProperties().add(p);
    }    
    
    private void saveInRem2(org.wiredwidgets.cow.server.api.model.v2.Process process, Definitions definitions) {
        Node node = new Node();
        node.setType("rem:marketplace");
        node.setName(process.getName());
        
        addProperty(node, "rem:name", process.getName());
        addProperty(node, "rem:description", process.getName());
        addProperty(node, "rem:type", "workflow");
        
        Node content = new Node();
        content.setName("jcr:content");
        content.setType("nt:resource");
        
        StringWriter sw = new StringWriter();
        marshaller.marshal(definitions, new StreamResult(sw));
        
        addProperty(content, "jcr:data", sw.toString()); 
        addProperty(content, "jcr:mimeType", "application/xml");
        node.getNodes().add(content);
             
        log.info("calling rem2");
        RestTemplate restTemplate = new RestTemplate();
        URI location = restTemplate.postForLocation("http://scout.mitre.org:8080/rem2/cms/workflows", node);
    } 
    
    private InputStream marshalToInputStream(Object source) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        marshaller.marshal(source, new StreamResult(out));
        return new ByteArrayInputStream(out.toByteArray());

    }
  
}
