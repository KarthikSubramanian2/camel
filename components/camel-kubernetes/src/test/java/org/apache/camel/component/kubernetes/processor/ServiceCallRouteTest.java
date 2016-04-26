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

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ServiceCallConfigurationDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ServiceCallRouteTest extends CamelTestSupport {

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
                ServiceCallConfigurationDefinition config = new ServiceCallConfigurationDefinition();
                config.setMasterUrl("https://fabric8-master.vagrant.f8:8443");
                config.setUsername("admin");
                config.setPassword("admin");
//                config.setOauthToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImZhYnJpYzgtdG9rZW4tZzNsdGoiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZmFicmljOCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImU0NGJhYzA0LWZmYjQtMTFlNS05MWM0LTA4MDAyN2I1YzJmNCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmZhYnJpYzgifQ.yqhevtuqliAV7RlRhaSjG8oFSOn2V1vfmj5V9JKpaOCWbWXMYS0y_v4QPfI4vIGsJtpZgasrt-8brkiOkq7zx0BJxVm-Ae5QIE1uJNeWFYcno823SUV2ebHykhp0eUEtCmWtHByBIoTTF8dG3NZ6jWow7KVGN289Y2ryi8QoYupfQ9ABddVVcduolStIqBu3pu-dJqIvlt6L8wE6AHfhS4uSaPwcimbs5hrg6gB_iONCSCSayhOyiT6fNlXdpxndRRBg9MP3X3f4dD3kDyHE0860HzqZ05jFIwGfV_rbFJeNY3SLDQNO_QFXqUZKg01OH-OJaqDSjuV48P9b6n4uHA");
                config.setNamespace("default");

                from("direct:start")
                    .serviceCall("cdi-camel-jetty", "http:cdi-camel-jetty", config)
                    .to("mock:result");
            }
        };
    }
}
