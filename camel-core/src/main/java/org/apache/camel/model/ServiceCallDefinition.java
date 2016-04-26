/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteContext;

@Metadata(label = "eip,routing")
@XmlRootElement(name = "serviceCall")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCallDefinition extends NoOutputDefinition<ServiceCallDefinition> {

    // TODO: load balancing strategy

    @XmlElement
    private ServiceCallConfigurationDefinition serviceCallConfiguration;
    @XmlAttribute @Metadata(required = "true")
    private String uri;
    @XmlAttribute
    private ExchangePattern pattern;
    @XmlAttribute @Metadata(defaultValue = "default")
    private String namespace;
    @XmlAttribute @Metadata(required = "true")
    private String name;
    @XmlAttribute
    private String discovery;
    @XmlAttribute
    private String serviceCallConfigurationRef;

    public ServiceCallDefinition() {
    }

    // serviceCall("myService") (will use http by default)
    // serviceCall("myService/foo") (will use http by default)
    // serviceCall("http:myService/foo")
    // serviceCall("myService", "http:myService.host:myService.port/foo")
    // serviceCall("myService", "netty4:tcp:myService?connectTimeout=1000")

    @Override
    public String toString() {
        return "ServiceCall[" + name + "]";
    }

    @Override
    public String getLabel() {
        return "serviceCall";
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        if (discovery != null) {
            throw new IllegalStateException("Cannot find Camel component on the classpath implementing the discovery provider: " + discovery);
        } else {
            throw new IllegalStateException("Cannot find Camel component supporting the ToService EIP. Add camel-kubernetes if you are using Kubernetes.");
        }
    }

    // Fluent API
    // -------------------------------------------------------------------------

    /**
     * Sets the optional {@link ExchangePattern} used to invoke this endpoint
     */
    public ServiceCallDefinition pattern(ExchangePattern pattern) {
        setPattern(pattern);
        return this;
    }

    /**
     * Sets the namespace of the service to use
     */
    public ServiceCallDefinition namespace(String namespace) {
        setNamespace(namespace);
        return this;
    }

    /**
     * Sets the name of the service to use
     */
    public ServiceCallDefinition name(String name) {
        setName(name);
        return this;
    }

    /**
     * Sets the discovery provided to use.
     * <p/>
     * Use kubernetes to use kubernetes.
     * Use ribbon to use ribbon.
     */
    public ServiceCallDefinition discovery(String discovery) {
        setDiscovery(discovery);
        return this;
    }

    public ServiceCallConfigurationDefinition serviceCallConfiguration() {
        serviceCallConfiguration = new ServiceCallConfigurationDefinition(this);
        return serviceCallConfiguration;
    }

    /**
     * Configures the ServiceCall using the given configuration
     */
    public ServiceCallDefinition serviceCallConfiguration(ServiceCallConfigurationDefinition configuration) {
        serviceCallConfiguration = configuration;
        return this;
    }

    /**
     * Refers to a ServiceCall configuration to use
     */
    public ServiceCallDefinition serviceCallConfiguration(String ref) {
        serviceCallConfigurationRef = ref;
        return this;
    }

    // Properties
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public ExchangePattern getPattern() {
        return pattern;
    }

    public void setPattern(ExchangePattern pattern) {
        this.pattern = pattern;
    }

    public String getDiscovery() {
        return discovery;
    }

    public void setDiscovery(String discovery) {
        this.discovery = discovery;
    }

    public ServiceCallConfigurationDefinition getServiceCallConfiguration() {
        return serviceCallConfiguration;
    }

    public void setServiceCallConfiguration(ServiceCallConfigurationDefinition serviceCallConfiguration) {
        this.serviceCallConfiguration = serviceCallConfiguration;
    }

    public String getServiceCallConfigurationRef() {
        return serviceCallConfigurationRef;
    }

    public void setServiceCallConfigurationRef(String serviceCallConfigurationRef) {
        this.serviceCallConfigurationRef = serviceCallConfigurationRef;
    }

    public String getUri() {
        return uri;
    }

    /**
     * The uri of the endpoint to send to.
     * The uri can be dynamic computed using the {@link org.apache.camel.language.simple.SimpleLanguage} expression.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
