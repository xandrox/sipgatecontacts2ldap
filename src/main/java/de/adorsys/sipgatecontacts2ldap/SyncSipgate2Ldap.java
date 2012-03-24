package de.adorsys.sipgatecontacts2ldap;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.data.ParserException;

@Service
public class SyncSipgate2Ldap {

	@Autowired
	private SipGateService sipGateService;
	
	@Autowired
	private LdapUserDao ldapUserDao;

	public void setSipGateService(SipGateService sipGateService) {
		this.sipGateService = sipGateService;
	}
	public void setLdapUserDao(LdapUserDao ldapUserDao) {
		this.ldapUserDao = ldapUserDao;
	}

	public void sync() throws IOException, ParserException {
		List<ContactPerson> addressbook = sipGateService
				.getAddressbookAsVCard();
		ldapUserDao.importContacts(addressbook);
		System.out.println("finished");

	}


}
