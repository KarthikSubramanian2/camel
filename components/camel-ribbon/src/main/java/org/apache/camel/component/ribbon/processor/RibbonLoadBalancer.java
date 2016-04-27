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
package org.apache.camel.component.ribbon.processor;

import java.util.Collection;

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.apache.camel.spi.ServiceCallLoadBalancer;

/**
 * Ribbon {@link IRule} based load balancer.
 */
@Deprecated
public class RibbonLoadBalancer implements ServiceCallLoadBalancer<RibbonServer>, IRule {

    private final IRule rule;

    public RibbonLoadBalancer(IRule rule) {
        this.rule = rule;
    }

    @Override
    public RibbonServer chooseServer(Collection<RibbonServer> servers) {
        Server server = choose(null);
        if (server != null) {
            return new RibbonServer(server.getHost(), server.getPort());
        } else {
            return null;
        }
    }

    @Override
    public Server choose(Object key) {
        return rule.choose(key);
    }

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        rule.setLoadBalancer(lb);
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return rule.getLoadBalancer();
    }
}
