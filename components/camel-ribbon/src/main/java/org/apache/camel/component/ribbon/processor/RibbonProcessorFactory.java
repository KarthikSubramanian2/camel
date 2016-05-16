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
package org.apache.camel.component.ribbon.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netflix.loadbalancer.IRule;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.component.ribbon.RibbonConfiguration;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.ServiceCallConfigurationDefinition;
import org.apache.camel.model.ServiceCallDefinition;
import org.apache.camel.spi.ProcessorFactory;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.ServiceCallServerListStrategy;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;

/**
 * {@link ProcessorFactory} that creates the Ribbon implementation of the ServiceCall EIP.
 */
public class RibbonProcessorFactory implements ProcessorFactory {

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

            String name = sc.getName();
            String namespace = sc.getNamespace();
            String uri = sc.getUri();
            ExchangePattern mep = sc.getPattern();

            ServiceCallConfigurationDefinition config = sc.getServiceCallConfiguration();
            ServiceCallConfigurationDefinition configRef = null;
            if (sc.getServiceCallConfigurationRef() != null) {
                configRef = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), sc.getServiceCallConfigurationRef(), ServiceCallConfigurationDefinition.class);
            }

            // if no configuration explicit configured then try to lookup in registry by type and find the best candidate to use
            if (config == null && configRef == null) {
                Set<ServiceCallConfigurationDefinition> set = routeContext.getCamelContext().getRegistry().findByType(ServiceCallConfigurationDefinition.class);
                if (set != null) {
                    for (ServiceCallConfigurationDefinition candidate : set) {
                        if (candidate.getComponent() == null || "kubernetes".equals(candidate.getComponent())) {
                            config = candidate;
                            break;
                        }
                    }
                }
            }

            // extract the properties from the configuration from the model
            Map<String, Object> parameters = new HashMap<>();
            if (configRef != null) {
                IntrospectionSupport.getProperties(configRef, parameters, null);
            }
            if (config != null) {
                IntrospectionSupport.getProperties(config, parameters, null);
            }

            // component must either not be set, or if set then must be us
            String component = config != null ? config.getComponent() : null;
            if (component == null && configRef != null) {
                component = configRef.getComponent();
            }
            if (component != null && !"ribbon".equals(component)) {
                return null;
            }

            // and set them on the kubernetes configuration class
            RibbonConfiguration rc = new RibbonConfiguration();
            IntrospectionSupport.setProperties(rc, parameters);

            // use namespace from config if not provided
            if (namespace == null) {
                namespace = rc.getNamespace();
            }

            // lookup the load balancer to use (configured on EIP takes precedence vs configured on configuration)
            Object lb = configureLoadBalancer(routeContext, sc);
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

            // must be a ribbon load balancer
            if (lb != null && !(lb instanceof IRule)) {
                throw new IllegalArgumentException("Load balancer must be of type: " + IRule.class + " but is of type: " + lb.getClass().getName());
            }

            RibbonServiceCallProcessor processor = new RibbonServiceCallProcessor(name, namespace, uri, mep, rc);
            processor.setRule((IRule) lb);
            processor.setServerListStrategy(sl);
            return processor;
        } else {
            return null;
        }
    }

    private Object configureLoadBalancer(RouteContext routeContext, ServiceCallDefinition sd) {
        Object lb = null;

        if (sd != null) {
            lb = sd.getLoadBalancer();
            if (lb == null && sd.getLoadBalancerRef() != null) {
                String ref = sd.getLoadBalancerRef();
                lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ref);
            }
        }

        return lb;
    }

    private Object configureLoadBalancer(RouteContext routeContext, ServiceCallConfigurationDefinition config) {
        Object lb = config.getLoadBalancer();
        if (lb == null && config.getLoadBalancerRef() != null) {
            String ref = config.getLoadBalancerRef();
            lb = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ref);
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

