package de.adorsys.sipgatecontacts2ldap;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.parameter.Type;
import net.fortuna.ical4j.vcard.property.Address;
import net.fortuna.ical4j.vcard.property.Email;
import net.fortuna.ical4j.vcard.property.N;
import net.fortuna.ical4j.vcard.property.Org;
import net.fortuna.ical4j.vcard.property.Telephone;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextAdapter;

@XmlRootElement
public class ContactPerson {

	private String uid;
	private String hash;
	private String firstName;
	private String lastName;
	private String organization;
	private String emailWork;
	private String emailHome;
	
	private final List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
	private final List<ContactAddress> addresses = new ArrayList<ContactAddress>();


	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public static ContactPerson fromVCard(String vCardString, String sipgateId, String sipgateHash) throws IOException, ParserException {
		VCardBuilder vCardBuilder = new VCardBuilder(new StringReader(vCardString));
		VCard vCard = vCardBuilder.build();
		ContactPerson addressbookPerson = new ContactPerson();
		addressbookPerson.hash = sipgateHash;
		addressbookPerson.uid = sipgateId;
		N name = (N) vCard.getProperty(Id.N);
		addressbookPerson.setFirstName(name.getGivenName());
		addressbookPerson.setLastName(name.getFamilyName());

		Org org = (Org) vCard.getProperty(Id.ORG);
		if (org != null) {
			addressbookPerson.organization = StringUtils.trimToNull(org.getValue());
		}
		
		List<Property> telephoneProperties = vCard.getProperties(Id.TEL);
		for (Property property : telephoneProperties) {
			Telephone tel = (Telephone) property;
			addressbookPerson.getPhoneNumbers().add(new PhoneNumber(tel));
		}
		
		List<Property> properties = vCard.getProperties(Id.ADR);
		for (Property property : properties) {
			net.fortuna.ical4j.vcard.property.Address address = (Address) property;
			ContactAddress contactAddress = new ContactAddress(address);
			addressbookPerson.addresses.add(contactAddress);
		}
		
		List<Property> emailProperties = vCard.getProperties(Id.EMAIL);
		for (Property property : emailProperties) {
			Email email = (Email) property;
			Type type = (Type) email.getParameter(net.fortuna.ical4j.vcard.Parameter.Id.TYPE);
			if (Arrays.asList(type.getTypes()).contains(Type.WORK.getValue())) {
				addressbookPerson.emailWork = email.getValue();
			} else 	if (Arrays.asList(type.getTypes()).contains(Type.HOME.getValue())) {
				addressbookPerson.emailHome = email.getValue();
			}
		}
		return addressbookPerson;
	}
	
	public void mapToContext(DirContextAdapter context) {
		context.setAttributeValues("objectclass", new String[] { "top",
				"inetorgperson", "adorsys-sipgate-hash" });
		context.setAttributeValue("cn",
				this.getCN());
		context.setAttributeValue("givenName", this.getFirstName());
		context.setAttributeValue("sn", this.getLastName());
		
		if (this.emailWork != null) {
			context.setAttributeValue("mail", this.emailWork);
		} else if (this.emailHome != null) {
			context.setAttributeValue("mail", this.emailHome);
		}
		if (this.organization != null) {
			context.setAttributeValue("o", this.organization);
		}
		context.setAttributeValue("ad-sipgate-hash", this.hash);
		for (PhoneNumber phone : phoneNumbers) {
			phone.mapToContext(context);
		}
		
		for (ContactAddress address : addresses) {
			address.mapToContext(context);
		}
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uid == null) ? 0 : uid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactPerson other = (ContactPerson) obj;
		if (uid == null) {
			if (other.uid != null)
				return false;
		} else if (!uid.equals(other.uid))
			return false;
		return true;
	}

	public String getCN() {
		return getFirstName() + " " + getLastName();
	}

	public List<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
	}
	
}
