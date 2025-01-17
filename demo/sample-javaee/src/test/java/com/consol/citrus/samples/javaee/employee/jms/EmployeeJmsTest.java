/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.javaee.employee.jms;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.io.IOException;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.arquillian.shrinkwrap.CitrusArchiveBuilder;
import com.consol.citrus.jms.endpoint.JmsEndpoints;
import com.consol.citrus.jms.endpoint.JmsSyncEndpoint;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.javaee.Deployments;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.connection.SingleConnectionFactory;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;

@RunWith(Arquillian.class)
public class EmployeeJmsTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @Resource(mappedName = "java:/jms/queue/employee")
    private Queue employeeQueue;

    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;

    private JmsSyncEndpoint employeeJmsEndpoint;

    @Deployment
    public static WebArchive createDeployment() throws IOException {
        return Deployments.employeeJmsRegistry()
                    .addAsResource(new ClassPathResource("wsdl/SmsGateway.wsdl").getFile(), new BasicPath("/wsdl/SmsGateway.wsdl"))
                    .addAsLibraries(CitrusArchiveBuilder.latestVersion().core().javaDsl().http().mail().ws().jms().build());
    }

    @Before
    public void setUp() {
        employeeJmsEndpoint = JmsEndpoints.jms().synchronous()
                                    .destination(employeeQueue)
                                    .connectionFactory(new SingleConnectionFactory(connectionFactory))
                                    .build();
    }

    @After
    public void cleanUp() {
        closeConnections();
    }

    @Test
    @CitrusTest
    public void testAdd(@CitrusResource TestCaseRunner citrus) {
        citrus.run(send()
            .endpoint(employeeJmsEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .header("name", "Amy")
            .header("age", 20));

        citrus.run(receive()
            .endpoint(employeeJmsEndpoint)
            .message()
            .type(MessageType.PLAINTEXT)
            .body("Successfully created employee: Amy(20)")
            .header("success", true));
    }

    private void closeConnections() {
        ((SingleConnectionFactory) employeeJmsEndpoint.getEndpointConfiguration().getConnectionFactory()).destroy();
    }
}
