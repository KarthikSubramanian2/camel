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
package org.apache.camel.component.flink.springboot;

import org.apache.camel.component.flink.DataSetCallback;
import org.apache.camel.component.flink.DataStreamCallback;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * The flink component can be used to send DataSet jobs to Apache Flink cluster.
 * 
 * Generated by camel-package-maven-plugin - do not edit this file!
 */
@ConfigurationProperties(prefix = "camel.component.flink")
public class FlinkComponentConfiguration {

    /**
     * DataSet to compute against.
     */
    @NestedConfigurationProperty
    private DataSet dataSet;
    /**
     * DataStream to compute against.
     */
    @NestedConfigurationProperty
    private DataStream dataStream;
    /**
     * Function performing action against a DataSet.
     */
    @NestedConfigurationProperty
    private DataSetCallback dataSetCallback;
    /**
     * Function performing action against a DataStream.
     */
    @NestedConfigurationProperty
    private DataStreamCallback dataStreamCallback;

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public DataStream getDataStream() {
        return dataStream;
    }

    public void setDataStream(DataStream dataStream) {
        this.dataStream = dataStream;
    }

    public DataSetCallback getDataSetCallback() {
        return dataSetCallback;
    }

    public void setDataSetCallback(DataSetCallback dataSetCallback) {
        this.dataSetCallback = dataSetCallback;
    }

    public DataStreamCallback getDataStreamCallback() {
        return dataStreamCallback;
    }

    public void setDataStreamCallback(DataStreamCallback dataStreamCallback) {
        this.dataStreamCallback = dataStreamCallback;
    }
}