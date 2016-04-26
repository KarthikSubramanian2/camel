/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;

public class KubernetesProcessorFactory implements ProcessorFactory {

    @Override
    public Processor createChildProcessor(RouteContext routeContext, ProcessorDefinition<?> definition, boolean mandatory) throws Exception {
        // not in use
        return null;
    }

    @Override
    public Processor createProcessor(RouteContext routeContext, ProcessorDefinition<?> definition) throws Exception {
        if (definition instanceof ServiceCallDefinition) {
            ServiceCallDefinition ts = (ServiceCallDefinition) definition;

            // discovery must either not be set, or if set then must be us
            if (ts.getDiscovery() != null && !"kubernetes".equals(ts.getDiscovery())) {
                return null;
            }

            String name = ts.getName();
            String namespace = ts.getNamespace();
            String uri = ts.getUri();
            ExchangePattern mep = ts.getPattern();

            ServiceCallConfigurationDefinition config = ts.getServiceCallConfiguration();
            ServiceCallConfigurationDefinition configRef = null;
            if (ts.getServiceCallConfigurationRef() != null) {
                configRef = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), ts.getServiceCallConfigurationRef(), ServiceCallConfigurationDefinition.class);
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

            // TODO: allow to specify kubernetes/openshift etc

            // use namespace from config if not provided
            if (namespace == null) {
                namespace = kc.getNamespace();
            }

            return new KubernetesServiceProcessor(name, namespace, uri, mep, kc);
        } else {
            return null;
        }
    }

}
