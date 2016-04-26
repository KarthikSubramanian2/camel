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
package org.apache.camel.component.kubernetes.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.kubernetes.KubernetesConfiguration;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ServiceCallConfigurationDefinition;
import org.apache.camel.model.ServiceCallDefinition;
import org.apache.camel.spi.ProcessorFactory;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.ServiceCallLoadBalancer;
import org.apache.camel.spi.ServiceCallServerListStrategy;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;

/**
 * {@link ProcessorFactory} that creates the Kubernetes implementation of the ServiceCall EIP.
 */
public class KubernetesProcessorFactory implements ProcessorFactory {

    @Override
    public Processor createChildProcessor(RouteContext routeContext, ProcessorDefinition<?> definition, boolean mandatory) throws Exception {
        // not in use
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Processor createProcessor(RouteContext routeContext, ProcessorDefinition<?> definition) throws Exception {
        if (definition instanceof ServiceCallDefinition) {
            ServiceCallDefinition sc = (ServiceCallDefinition) definition;

            // discovery must either not be set, or if set then must be us
            if (sc.getDiscovery() != null && !"kubernetes".equals(sc.getDiscovery())) {
                return null;
            }

            String name = sc.getName();
            String namespace = sc.getNamespace();
            String uri = sc.getUri();
            ExchangePattern mep = sc.getPattern();

            ServiceCallConfigurationDefinition config = sc.getServiceCallConfiguration();
            ServiceCallConfigurationDefinition configRef = null;
            if (sc.getServiceCallConfigurationRef() != null) {
                configRef = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), sc.getServiceCallConfigurationRef(), ServiceCallConfigurationDefinition.class);
            }

            // extract the properties from the configuration from the model
            Map<String, Object> parameters = new HashMap<>();
            if (configRef != null) {
                IntrospectionSupport.getProperties(configRef, parameters, null);
            }
            if (config != null) {
                IntrospectionSupport.getProperties(config, parameters, null);
            }
            // and set them on the kubernetes configuration class
            KubernetesConfiguration kc = new KubernetesConfiguration();
            IntrospectionSupport.setProperties(kc, parameters);

            // use namespace from config if not provided
            if (namespace == null) {
                namespace = kc.getNamespace();
            }

            // lookup the load balancer to use (configured on EIP takes precedence vs configured on configuration)
            ServiceCallLoadBalancer lb = configureLoadBalancer(routeContext, sc);
            if (lb == null && config != null) {
                lb = configureLoadBalancer(routeContext, config);
            }
            if (lb == null && configRef != null) {
                lb = configureLoadBalancer(routeContext, configRef);
            }

            // lookup the server list strategy to use (configured on EIP takes precedence vs configured on configuration)
            ServiceCallServerListStrategy sl = configureServerListStrategy(routeContext, sc);
            if (sl == null && config != null) {
                sl = configureServerListStrategy(routeContext, config);
            }
            if (sl == null && configRef != null) {
                sl = configureServerListStrategy(routeContext, configRef);
            }

            KubernetesServiceCallProcessor processor = new KubernetesServiceCallProcessor(name, namespace, uri, mep, kc);
            processor.setLoadBalancer(lb);
            processor.setServerListStrategy(sl);
            return processor;
        } else {
            return null;
        }
    }

    private ServiceCallLoadBalancer configureLoadBalancer(RouteContext routeContext, ServiceCallDefinition sd) {
        ServiceCallLoadBalancer lb = null;

        if (sd != null) {
            lb = sd.getLoadBalancer();
            if (lb == null && sd.getLoadBalancerRef() != null) {
                String ref = sd.getLoadBalancerRef();
                // special for ref is referring to built-in
                if ("random".equalsIgnoreCase(ref)) {
                    lb = new RandomLoadBalancer();
                } else if ("roundrobin".equalsIgnoreCase(ref)) {
                    lb = new RoundRobinBalancer();
                } else {
                    lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ref, ServiceCallLoadBalancer.class);
                }
            }
        }

        return lb;
    }

    private ServiceCallLoadBalancer configureLoadBalancer(RouteContext routeContext, ServiceCallConfigurationDefinition config) {
        ServiceCallLoadBalancer lb = config.getLoadBalancer();
        if (lb == null && config.getLoadBalancerRef() != null) {
            String ref = config.getLoadBalancerRef();
            // special for ref is referring to built-in
            if ("random".equalsIgnoreCase(ref)) {
                lb = new RandomLoadBalancer();
            } else if ("roundrobin".equalsIgnoreCase(ref)) {
                lb = new RoundRobinBalancer();
            } else {
                lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ref, ServiceCallLoadBalancer.class);
            }
        }
        return lb;
    }

    private ServiceCallServerListStrategy configureServerListStrategy(RouteContext routeContext, ServiceCallDefinition sd) {
        ServiceCallServerListStrategy lb = null;

        if (sd != null) {
            lb = sd.getServerListStrategy();
            if (lb == null && sd.getServerListStrategyRef() != null) {
                lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), sd.getServerListStrategyRef(), ServiceCallServerListStrategy.class);
            }
        }

        return lb;
    }

    private ServiceCallServerListStrategy configureServerListStrategy(RouteContext routeContext, ServiceCallConfigurationDefinition config) {
        ServiceCallServerListStrategy lb = config.getServerListStrategy();
        if (lb == null && config.getServerListStrategyRef() != null) {
            String ref = config.getServerListStrategyRef();
            lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ref, ServiceCallServerListStrategy.class);
        }
        return lb;
    }

}
