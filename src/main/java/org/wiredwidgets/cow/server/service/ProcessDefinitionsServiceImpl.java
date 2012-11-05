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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.wiredwidgets.cow.server.api.service.ProcessDefinition;

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;

/**
 *
 * @author JKRANES
 */
@Transactional
@Component
public class ProcessDefinitionsServiceImpl extends AbstractCowServiceImpl implements ProcessDefinitionsService {
	
	@Autowired
	RestTemplate restTemplate;

    // private static TypeDescriptor JBPM_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(org.jbpm.api.ProcessDefinition.class));
    private static TypeDescriptor COW_PROCESS_DEFINITION_LIST = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(ProcessDefinition.class));
    
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findAllProcessDefinitions() {
        // return this.convertProcessDefinitions(repositoryService.createProcessDefinitionQuery().list());
    	throw new UnsupportedOperationException("Not supported yet.");
    }
    

    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findProcessDefinitionsByKey(String key) {
        // return this.convertProcessDefinitions(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list());
    	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean deleteProcessDefinitionsByKey(String key) {
//        boolean found = false;
//        for (org.jbpm.api.ProcessDefinition def : repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list()) {
//            found = true;
//            repositoryService.deleteDeployment(def.getDeploymentId());
//        }
//        return found;
    	throw new UnsupportedOperationException("Not supported yet.");
    }    

    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition findLatestVersionProcessDefinitionByKey(String key) {
//        List<org.jbpm.api.ProcessDefinition> defs = this.filterLatestVersions(repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list());
//        return (defs.isEmpty()? null : this.converter.convert(defs.get(0), ProcessDefinition.class) );
    	throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Finds the latest version of each process definition
     *
     * @return
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProcessDefinition> findLatestVersionProcessDefinitions() {
        // return this.convertProcessDefinitions(this.filterLatestVersions(repositoryService.createProcessDefinitionQuery().list()));	
    	return getDefsFromRem2();
    }



    @Transactional(readOnly = true)
    @Override
    public ProcessDefinition getProcessDefinition(String id) {
        // return this.converter.convert(repositoryService.createProcessDefinitionQuery().processDefinitionId(id).uniqueResult(), ProcessDefinition.class);
    	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private List<ProcessDefinition> getDefsFromRem2() {
    	List<ProcessDefinition> defs = new ArrayList<ProcessDefinition>();
    	
    	String url = REM2_URL + "/search/rem.rss?where=parent.[rem:type]%3D'workflow'";
    	URI uri;
    	try {
    		uri = new URI(url);    		
    	}
    	catch (URISyntaxException e) {
    		throw new RuntimeException("Invalid URI: " + url);
    	}
    	Channel channel = restTemplate.getForObject(uri, Channel.class);
    	// getItems() returns an untyped List
    	for(Object itemObj : channel.getItems()){
    		Item item = (Item)itemObj;
    		ProcessDefinition pd = new ProcessDefinition();
    		pd.setName(item.getTitle());
    		pd.setKey(item.getTitle());
    		defs.add(pd);
    	}
    	return defs;
    	
    }

    
    
    
}
