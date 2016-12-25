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

package org.apache.camel.component.consul.processor.remote;

import org.apache.camel.ExchangePattern;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.impl.remote.DefaultServiceCallProcessor;
import org.apache.camel.spi.ProcessorFactory;
import org.apache.camel.spi.ServiceCallServer;
import org.apache.camel.spi.ServiceCallServerListStrategy;

/**
 * {@link ProcessorFactory} that creates the Consul implementation of the ServiceCall EIP.
 */
public class ConsulServiceCallProcessor extends DefaultServiceCallProcessor<ServiceCallServer> {
    public ConsulServiceCallProcessor(String name, String scheme, String uri, ExchangePattern exchangePattern, ConsulConfiguration conf) {
        super(name, scheme, uri, exchangePattern);
    }

    @Override
    public void setServerListStrategy(ServiceCallServerListStrategy<ServiceCallServer> serverListStrategy) {
        if (!(serverListStrategy instanceof ConsulServiceCallServerListStrategy)) {
            throw new IllegalArgumentException("ServerListStrategy is not an instance of ConsulServiceCallServerListStrategy");
        }

        super.setServerListStrategy(serverListStrategy);
    }
}
