/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.spi;

/**
 * Configuration use by calling remote services where the service is looked up in a service registry of some sorts,
 * implemented by Camel components to support the Camel {@link org.apache.camel.model.remote.ServiceCallDefinition Service Call} EIP.
 */
@Deprecated
public class ServiceCallConfiguration {

    private String component;
    private ServiceCallLoadBalancer loadBalancer;
    private ServiceCallServerListStrategy serverListStrategy;

    public String getComponent() {
        return component;
    }

    /**
     * Sets the name of the Camel component to use such as ribbon or kubernetes
     */
    public void setComponent(String component) {
        this.component = component;
    }

    public ServiceCallLoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    /**
     * Sets a custom {@link org.apache.camel.spi.ServiceCallLoadBalancer} load balancer to use.
     */
    public void setLoadBalancer(ServiceCallLoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ServiceCallServerListStrategy getServerListStrategy() {
        return serverListStrategy;
    }

    /**
     * Sets a custom {@link org.apache.camel.spi.ServiceCallServerListStrategy} strategy to obtain the list of active
     * servers that provides the service which is being selected by the load balancer when calling the remote service.
     */
    public void setServerListStrategy(ServiceCallServerListStrategy serverListStrategy) {
        this.serverListStrategy = serverListStrategy;
    }

}
