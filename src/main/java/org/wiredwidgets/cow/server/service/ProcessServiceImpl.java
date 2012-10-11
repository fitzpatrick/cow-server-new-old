/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wiredwidgets.cow.server.service;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceConfiguration;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.springframework.transaction.annotation.Transactional;
import org.wiredwidgets.cow.server.api.model.v2.Process;
import org.wiredwidgets.cow.server.api.service.Deployment;
import org.wiredwidgets.cow.server.api.service.ResourceNames;
import org.wiredwidgets.cow.server.parse.BPMN2SaxParser;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 *
 * @author FITZPATRICK
 */
@Transactional
public class ProcessServiceImpl extends AbstractCowServiceImpl implements ProcessService {

    private static Logger log = Logger.getLogger(ProcessServiceImpl.class);

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
        throw new UnsupportedOperationException("Not supported yet.");
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
}
