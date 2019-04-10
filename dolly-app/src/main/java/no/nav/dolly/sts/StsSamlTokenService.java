package no.nav.dolly.sts;

import static no.nav.dolly.properties.Environment.convertEnv;

import java.util.HashMap;
import java.util.Map;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.nav.dolly.properties.CredentialsProps;
import no.nav.dolly.properties.Environment;

@Service
public class StsSamlTokenService {

    private static final String STS_REQUEST_SAML_POLICY = "classpath:policy/requestSamlPolicy.xml";
    private static final String STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml";

    @Autowired
    private StsSamlFasitConsumer stsSamlFasitConsumer;

    @Autowired
    private CredentialsProps credentialsProps;

    public void configureStsRequestSamlToken(Object port, String env) {

        Client client = ClientProxy.getClient(port);
        STSClient stsClient = new STSClient(client.getBus());
        configureSTSClient(stsClient, convertEnv(env));

        client.getRequestContext().put(SecurityConstants.STS_CLIENT, stsClient);
        //Using CXF cache
        client.getRequestContext().put(SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT, true);
        Policy policy = resolvePolicyReference(client);
        setClientEndpointPolicy(client, policy);
    }

    private void configureSTSClient(STSClient stsClient, Environment env) {

        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);
        stsClient.setLocation(stsSamlFasitConsumer.getStsSamlService(env));

        Map<String, Object> properties = new HashMap<>();
        properties.put(SecurityConstants.USERNAME, credentialsProps.getUsername(env));
        properties.put(SecurityConstants.PASSWORD, credentialsProps.getPassword(env));

        stsClient.setProperties(properties);

        //used for the STS client to authenticate itself to the STS provider.
        stsClient.setPolicy(STS_CLIENT_AUTHENTICATION_POLICY);
    }

    private static Policy resolvePolicyReference(Client client) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(StsSamlTokenService.STS_REQUEST_SAML_POLICY);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();

        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        SoapMessage message = new SoapMessage(Soap12.getInstance());
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
    }
}