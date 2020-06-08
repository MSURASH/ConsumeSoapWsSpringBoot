package com.java.client;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.xml.transform.StringSource;

import com.java.loaneligibility.Acknowledgement;
import com.java.loaneligibility.CustomerRequest;
import com.java.loaneligibility.ObjectFactory;

@Service
public class SoapClient extends WebServiceGatewaySupport{

	@Autowired
	private Jaxb2Marshaller marshaller;

	@Autowired
	private Environment environment;

	private WebServiceTemplate template;

	

	public Acknowledgement getLoanStatus(CustomerRequest request) {
		template = new WebServiceTemplate(marshaller);
		Acknowledgement res = null;

		res = (Acknowledgement) template
				.marshalSendAndReceive("http://localhost:9090/ws", request

						, new WebServiceMessageCallback() {

							public void doWithMessage(WebServiceMessage message) throws TransformerException {
								try {
									SoapHeader soapHeader = ((SoapMessage) message).getSoapHeader();
									ObjectFactory factory = new ObjectFactory();

									Map mapRequest = new HashMap();

									mapRequest.put("loginuser", environment.getProperty("soap.auth.username"));
									mapRequest.put("loginpass", environment.getProperty("soap.auth.password"));
									StringSubstitutor substitutor = new StringSubstitutor(mapRequest, "%(", ")");
									String finalXMLRequest = substitutor.replace(environment.getProperty("soap.auth.header"));
									StringSource headerSource = new StringSource(finalXMLRequest);
									Transformer transformer = TransformerFactory.newInstance().newTransformer();
									transformer.transform(headerSource, soapHeader.getResult());
									
									JAXBContext context = JAXBContext.newInstance(Acknowledgement.class);
									//Marshaller marshaller = context.createMarshaller();
									//marshaller.marshal(headerSource, soapHeader.getResult());

									
								} catch (SoapFaultClientException e) {
									System.out.println("ERROR : " + e);
								} catch (JAXBException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

				);
		return res;
	}

}
