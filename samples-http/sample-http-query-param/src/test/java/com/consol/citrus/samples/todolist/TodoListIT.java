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

package com.consol.citrus.samples.todolist;

import java.util.UUID;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusSpringSupport {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private HttpServer todoListServer;

    @Test
    @CitrusTest
    public void testAddTodo() {
        $(http()
            .client(todoClient)
            .send()
            .get("/api/todo")
            .fork(true)
            .queryParam("id", UUID.randomUUID().toString())
            .queryParam("title", "todo_0001")
            .queryParam("description", null));

        $(http()
            .server(todoListServer)
            .receive()
            .get("/api/todo")
            .queryParam("title", "todo_0001")
            .queryParam("description", "@ignore@"));

        $(http()
            .server(todoListServer)
            .respond(HttpStatus.FOUND));

        $(http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.FOUND));
    }

}
