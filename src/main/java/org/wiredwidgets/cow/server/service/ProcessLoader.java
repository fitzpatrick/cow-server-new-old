package org.wiredwidgets.cow.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Loads all processes at system startup.
 * Note we cannot just use an init method in ProcessServiceImpl, as this would
 * bypass the transactional proxy.  Since we invoke the method here using
 * the interface, it will be properly transactional.
 * @author JKRANES
 *
 */
@Component
public class ProcessLoader {
	
	@Autowired
	ProcessService service;
	
	public void init() {
		service.loadAllProcesses();
	}

}
