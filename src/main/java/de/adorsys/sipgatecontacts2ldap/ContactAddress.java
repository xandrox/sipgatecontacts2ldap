package de.adorsys.sipgatecontacts2ldap;

import org.apache.commons.lang.StringUtils;
import org.springframework.ldap.core.DirContextAdapter;

public class ContactAddress {
	private final String street;
	private final String postcode;
	private final String locality;
	private final String country;
	private final String region;
	private final String poBox;

	public ContactAddress(net.fortuna.ical4j.vcard.property.Address address) {
		street = address.getStreet();
		country = address.getCountry();
		poBox = address.getPoBox();
		postcode = address.getPostcode();
		region = address.getRegion();
		locality = address.getLocality();
	}
	
	public void mapToContext(DirContextAdapter context) {
		if(StringUtils.isNotEmpty(locality))
			context.setAttributeValue("localityName", locality);
		if(StringUtils.isNotEmpty(postcode))
			context.setAttributeValue("postalCode", postcode);
		if(StringUtils.isNotEmpty(street))
			context.setAttributeValue("street", street);
		if(StringUtils.isNotEmpty(poBox))
			context.setAttributeValue("postOfficeBox", poBox);
		if(StringUtils.isNotEmpty(region))
			context.setAttributeValue("st", region);
//		if(StringUtils.isNotEmpty(country))
//			context.setAttributeValue("countryName", country);
	}

}
