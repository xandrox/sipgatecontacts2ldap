package de.adorsys.sipgatecontacts2ldap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.data.ParserException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SipGateService {
	
	@Value("${sipgate.username}")
	private String basicUserName;
	
	@Value("${sipgate.password}")
	private String basicPassord;
	
	public List<ContactPerson> getAddressbookAsVCard() throws IOException, ParserException {
    	try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL("https://api.sipgate.net/RPC2"));
			config.setBasicUserName(basicUserName);
			config.setBasicPassword(basicPassord);
			
			XmlRpcClient client = new XmlRpcClient();
			client.setConfig(config);
			client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
			HashMap<String, String> args = new HashMap<String, String>();
			args.put("ClientName", "Addressbook Sync");
			args.put("ClientVersion", "0.1");
			args.put("ClientVendor", "Adorsys GmbH & CO. KG");
			Object[] params = new Object[]{args};
			Object foo = client.execute("samurai.ClientIdentify", params);
			List<ContactPerson> persons = new ArrayList<ContactPerson>();
			
			Map<String, Object> phonebookRequest = new HashMap<String, Object>();
			phonebookRequest.put("EntryIDList", new String[]{});
			Map<Object, Object> phonbook = (Map<Object, Object>) client.execute("samurai.PhonebookEntryGet", new Object[]{phonebookRequest});
			Object[] result = (Object[]) phonbook.get("EntryList");
			for (Object object : result) {
				Map<String, String> entry = (Map<String, String>) object;
				String vCard = entry.get("Entry").replaceAll("http\\\\:","http:").replaceAll("skype\\\\:","skype:");
				String hash = entry.get("EntryHash");
				String id = entry.get("EntryID");
				try {
					persons.add(ContactPerson.fromVCard(vCard, id, hash));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return persons;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (XmlRpcException e) {
			throw new RuntimeException(e);
		}
	}

	public void setBasicUserName(String basicUserName) {
		this.basicUserName = basicUserName;
	}

	public void setBasicPassord(String basicPassord) {
		this.basicPassord = basicPassord;
	}
	
	

}
