package de.adorsys.sipgatecontacts2ldap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.op4j.Op;
import org.op4j.functions.MapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

@Service
public class LdapUserDao {

	private static Log log = LogFactory.getLog(LdapUserDao.class);

	private static final String[] OBJECTCLASS = new String[] { "inetorgperson",
			"adorsys-sipgate-hash" };

	private static final String OU = "users";

	@Autowired()
	private LdapTemplate ldapTemplate;

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	private DistinguishedName buildDn(ContactPerson ldapUser) {
		return buildDn(ldapUser.getUid(), ldapUser.getCN());
	}

	private DistinguishedName buildDn(String uid, String cn) {
		//"cn=Foo+uid=asjhdjh"
		DistinguishedName userDN = new DistinguishedName("cn="+ cn + "+uid=" + uid);
		return userDN;
	}


	public void create(ContactPerson person) {
		DirContextAdapter context = new DirContextAdapter();
		person.mapToContext(context);
		DistinguishedName dn = buildDn(person);
		ldapTemplate.bind(dn, context, null);
	}

	public void update(ContactPerson person) {
		Name dn = buildDn(person);
		delete(person);
		create(person);
	}

	public void delete(ContactPerson person) {
		DistinguishedName dn = buildDn(person);
		ldapTemplate.unbind(dn);
	}

	public ContactPerson findByPrimaryKey(String uid, String cn) {
		Name dn = buildDn(uid, cn);
		return (ContactPerson) ldapTemplate.lookup(dn, getContextMapper());
	}

	public List<ContactPerson> findAll() {
		EqualsFilter filter = new EqualsFilter("objectclass", "inetorgperson");
		return ldapTemplate.search(DistinguishedName.EMPTY_PATH,
				filter.encode(), getContextMapper());
	}

	protected ContextMapper getContextMapper() {
		return new PersonContextMapper();
	}



	public void importContacts(List<ContactPerson> imports) {
		List<ContactPerson> currentObjects = findAll();
		
		Set<ContactPerson> updateSet = createUpdateSet(imports, currentObjects);
		
		Set<ContactPerson> deleteSet = new HashSet<ContactPerson>(currentObjects);
		deleteSet.removeAll(imports);
		
		Set<ContactPerson> insertSet = new HashSet<ContactPerson>(imports);
		insertSet.removeAll(currentObjects);
		
		for (ContactPerson addressbookPerson : updateSet) {
			update(addressbookPerson);
		}
		for (ContactPerson addressbookPerson : deleteSet) {
			delete(addressbookPerson);
		}
		for (ContactPerson addressbookPerson : insertSet) {
			create(addressbookPerson);
		}
	}

	protected Set<ContactPerson> createUpdateSet(List<ContactPerson> imports,
			List<ContactPerson> currentObjects) {
		Set<ContactPerson> updateSet = new HashSet<ContactPerson>(imports);
		updateSet.retainAll(currentObjects);
		MapBuilder<ContactPerson,String,ContactPerson> mapBuilder = new MapBuilder<ContactPerson, String, ContactPerson>() {
			@Override
			public String buildKey(ContactPerson person) {
				return person.getUid();
			}
			
			@Override
			public ContactPerson buildValue(ContactPerson person) {
				return person;
			}
		};
		Map<String,ContactPerson> index = Op.on(currentObjects).toMap(mapBuilder).get();
		Iterator<ContactPerson> iterator = updateSet.iterator();
		while (iterator.hasNext()) {
			ContactPerson contactPerson = iterator.next();
			if(contactPerson.getHash().equals(index.get(contactPerson.getUid()).getHash())) {
				iterator.remove();
			}
		}
		return updateSet;
	}

	private static class PersonContextMapper implements ContextMapper {
		@Override
		public Object mapFromContext(Object ctx) {
			DirContextAdapter context = (DirContextAdapter) ctx;
			ContactPerson person = new ContactPerson();
			person.setUid(context.getStringAttribute("uid"));
			person.setLastName(context.getStringAttribute("sn"));
			person.setFirstName(context.getStringAttribute("givenName"));
			person.setHash(context.getStringAttribute("ad-sipgate-hash"));
			return person;
		}
	}
}
