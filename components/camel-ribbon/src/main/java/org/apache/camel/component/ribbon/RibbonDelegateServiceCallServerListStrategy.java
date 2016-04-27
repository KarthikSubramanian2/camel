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
package org.apache.camel.component.ribbon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.ServerList;
import org.apache.camel.spi.ServiceCallServer;
import org.apache.camel.spi.ServiceCallServerListStrategy;

/**
 * To let an existing {@link ServiceCallServerListStrategy} by managed by Ribbon.
 */
public class RibbonDelegateServiceCallServerListStrategy extends AbstractServerList<RibbonServer> implements ServerList<RibbonServer> {

    // TODO: a plain ribbon server list without plugin for kube for now
    // and some unit tests with static server list etc
    // https://github.com/Netflix/ribbon/wiki/Working-with-load-balancers
    // https://github.com/Netflix/ribbon/wiki/Getting-Started
    // then later with a kube facade so the servers are looked up in k8s

    private IClientConfig clientConfig;
    private final ServiceCallServerListStrategy delegate;

    /**
     * Lets ribbon manage the {@link ServiceCallServerListStrategy}.
     */
    public RibbonDelegateServiceCallServerListStrategy(ServiceCallServerListStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RibbonServer> getInitialListOfServers() {
        Collection<ServiceCallServer> servers = delegate.getInitialListOfServers();
        if (servers == null || servers.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<RibbonServer> answer = new ArrayList<>(servers.size());
        for (ServiceCallServer server : servers) {
            RibbonServer ribbon = new RibbonServer(server.getIp(), server.getPort());
            answer.add(ribbon);
        }
        return answer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RibbonServer> getUpdatedListOfServers() {
        Collection<ServiceCallServer> servers = delegate.getUpdatedListOfServers();
        if (servers == null || servers.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        List<RibbonServer> answer = new ArrayList<>(servers.size());
        for (ServiceCallServer server : servers) {
            RibbonServer ribbon = new RibbonServer(server.getIp(), server.getPort());
            answer.add(ribbon);
        }
        return answer;
    }
}
