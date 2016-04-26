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

import java.util.List;
import java.util.Random;
import java.util.concurrent.RejectedExecutionException;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Traceable;
import org.apache.camel.component.kubernetes.KubernetesConfiguration;
import org.apache.camel.spi.IdAware;
import org.apache.camel.support.ServiceSupport;
import org.apache.camel.util.AsyncProcessorHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesServiceProcessor extends ServiceSupport implements AsyncProcessor, Traceable, IdAware {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesServiceProcessor.class);

    private String id;
    private final String name;
    private final String namespace;
    private final String uri;
    private final ExchangePattern exchangePattern;
    private final KubernetesConfiguration configuration;

    private KubernetesServiceDiscovery discovery;

    // TODO: allow to plugin custom load balancer like ribbon

    public KubernetesServiceProcessor(String name, String namespace, String uri, ExchangePattern exchangePattern, KubernetesConfiguration configuration) {
        this.name = name;
        this.namespace = namespace;
        this.uri = uri;
        this.exchangePattern = exchangePattern;
        this.configuration = configuration;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        // TODO: in try .. catch and the callback stuff

        List<Server> services = null;
        try {
            services = discovery.getUpdatedListOfServers();
            if (services == null || services.isEmpty()) {
                exchange.setException(new RejectedExecutionException("No active services with name " + name + " in namespace " + namespace));
            }
        } catch (Throwable e) {
            exchange.setException(e);
        }

        if (exchange.getException() != null) {
            callback.done(true);
            return true;
        }

        // what strategy to use? random
        int size = services.size();
        int ran = new Random().nextInt(size);
        Server server = services.get(ran);

        String ip = server.getIp();
        int port = server.getPort();

        LOG.debug("Random selected service {} active at: {}:{}", name, ip, port);

        // build uri based on the name


        // TODO: lookup service
        // TODO: apply LB strategy
        // TODO build uri
        callback.done(true);
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTraceLabel() {
        return "kubernetes";
    }

    @Override
    protected void doStart() throws Exception {
        ObjectHelper.notEmpty(name, "name", this);
        ObjectHelper.notEmpty(namespace, "namespace", this);
        ObjectHelper.notEmpty(configuration.getMasterUrl(), "masterUrl", this);

        discovery = new KubernetesServiceDiscovery(name, namespace, null, createKubernetesClient());
        ServiceHelper.startService(discovery);
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopService(discovery);
    }

    private OpenShiftClient createKubernetesClient() {
        // TODO: need to use OpenShiftClient until fabric8-client can auto detect OS vs Kube environment
        LOG.debug("Create Kubernetes client with the following Configuration: " + configuration.toString());

        ConfigBuilder builder = new ConfigBuilder();
        builder.withMasterUrl(configuration.getMasterUrl());
        if ((ObjectHelper.isNotEmpty(configuration.getUsername())
                && ObjectHelper.isNotEmpty(configuration.getPassword()))
                && ObjectHelper.isEmpty(configuration.getOauthToken())) {
            builder.withUsername(configuration.getUsername());
            builder.withPassword(configuration.getPassword());
        } else {
            builder.withOauthToken(configuration.getOauthToken());
        }
        if (ObjectHelper.isNotEmpty(configuration.getCaCertData())) {
            builder.withCaCertData(configuration.getCaCertData());
        }
        if (ObjectHelper.isNotEmpty(configuration.getCaCertFile())) {
            builder.withCaCertFile(configuration.getCaCertFile());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientCertData())) {
            builder.withClientCertData(configuration.getClientCertData());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientCertFile())) {
            builder.withClientCertFile(configuration.getClientCertFile());
        }
        if (ObjectHelper.isNotEmpty(configuration.getApiVersion())) {
            builder.withApiVersion(configuration.getApiVersion());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientKeyAlgo())) {
            builder.withClientKeyAlgo(configuration.getClientKeyAlgo());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientKeyData())) {
            builder.withClientKeyData(configuration.getClientKeyData());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientKeyFile())) {
            builder.withClientKeyFile(configuration.getClientKeyFile());
        }
        if (ObjectHelper.isNotEmpty(configuration.getClientKeyPassphrase())) {
            builder.withClientKeyPassphrase(configuration.getClientKeyPassphrase());
        }
        if (ObjectHelper.isNotEmpty(configuration.getTrustCerts())) {
            builder.withTrustCerts(configuration.getTrustCerts());
        }

        Config conf = builder.build();
        return new DefaultOpenShiftClient(conf);
    }

}
