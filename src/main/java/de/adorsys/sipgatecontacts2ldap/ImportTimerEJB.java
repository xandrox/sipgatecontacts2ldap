package de.adorsys.sipgatecontacts2ldap;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import net.fortuna.ical4j.data.ParserException;

import org.jboss.spring.vfs.context.VFSClassPathXmlApplicationContext;

@Singleton
@Startup
public class ImportTimerEJB {
	
	private SyncSipgate2Ldap syncSipgate2Ldap;
	
	@PostConstruct
	public void init() {
		VFSClassPathXmlApplicationContext vfsXmlWebApplicationContext = new VFSClassPathXmlApplicationContext("jboss-spring.xml", ImportTimerEJB.class);
		syncSipgate2Ldap = vfsXmlWebApplicationContext.getBean("syncSipgate2Ldap", SyncSipgate2Ldap.class);
	}

	@Schedule(hour = "*", minute = "1/10")
	public void transferContacts() throws IOException, ParserException {
		syncSipgate2Ldap.sync();
	}

}
