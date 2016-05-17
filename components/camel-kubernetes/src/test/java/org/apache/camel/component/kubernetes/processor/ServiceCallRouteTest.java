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

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.remote.KubernetesConfigurationDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Manual test")
public class ServiceCallRouteTest extends CamelTestSupport {

    private JndiRegistry registry;

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        registry = super.createRegistry();
        return registry;
    }

    @Test
    public void testServiceCall() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                KubernetesConfigurationDefinition config = new KubernetesConfigurationDefinition();
                config.setMasterUrl("https://fabric8-master.vagrant.f8:8443");
                config.setUsername("admin");
                config.setPassword("admin");
                config.setNamespace("default");
                // lets use the built-in round robin (random is default)
                config.setLoadBalancerRef("roundrobin");

                // add the config to the registry so service call can use it
                registry.bind("myConfig", config);

                from("direct:start")
                    .serviceCall("cdi-camel-jetty")
                    .serviceCall("cdi-camel-jetty")
                    .to("mock:result");
            }
        };
    }
}
