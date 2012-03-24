package de.adorsys.sipgatecontacts2ldap;

import net.fortuna.ical4j.vcard.Parameter.Id;
import net.fortuna.ical4j.vcard.property.Telephone;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextAdapter;


public class PhoneNumber {
	
	private final String number;
	private final Type type;
	
	public enum Type {
		BUSINESS("work", "telephoneNumber"),
		PRIVATE("", "homePhone"),
		MOBILE("cell", "mobile"),
		FAX_PRIVATE("", ""),
		FAX_BUSINESS("fax,work", "telexNumber"),
		PAGER("", "pager"),
		OTHER("", "description");
		
		private final String vCardType;
		private final String ldapAttr;
		
		Type(String vCardType, String ldapAttr) {
			this.vCardType = vCardType;
			this.ldapAttr = ldapAttr;
		}
		
		public boolean isVCardType(String type) {
			return vCardType.equals(type);
		}
		
		public static Type valueOf(Telephone tel) {
			net.fortuna.ical4j.vcard.parameter.Type type = (net.fortuna.ical4j.vcard.parameter.Type) tel.getParameter(Id.TYPE);
			for (Type t : Type.values()) {
				if (t.isVCardType(type.getValue())) {
					return t;
				}
			}
			return BUSINESS;
		}
	}
	
	public PhoneNumber(Telephone tel) {
		type = Type.valueOf(tel);
		number = tel.getUri().getSchemeSpecificPart();
	}

	public String getNumber() {
		return number;
	}

	public Type getType() {
		return type;
	}
	
	public void mapToContext(DirContextAdapter context) {
		if (StringUtils.isNotEmpty(number))
			context.addAttributeValue(getType().ldapAttr, getNumber());
	}

}
