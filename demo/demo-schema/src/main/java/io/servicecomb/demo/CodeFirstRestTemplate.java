/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.servicecomb.core.CseContext;
import io.servicecomb.demo.compute.Person;
import io.servicecomb.demo.server.User;
import io.servicecomb.serviceregistry.RegistryUtils;

public class CodeFirstRestTemplate {
    public void testCodeFirst(RestTemplate template, String microserviceName, String basePath) {
        for (String transport : DemoConst.transports) {
            CseContext.getInstance().getConsumerProviderManager().setTransport(microserviceName, transport);
            TestMgr.setMsg(microserviceName, transport);

            String cseUrlPrefix = "cse://" + microserviceName + basePath;

            testExtend(template, cseUrlPrefix);

            testCodeFirstUserMap(template, cseUrlPrefix);
            testCodeFirstTextPlain(template, cseUrlPrefix);
            testCodeFirstBytes(template, cseUrlPrefix);
            testCseResponse(microserviceName, template, cseUrlPrefix);
            testCodeFirstAddDate(template, cseUrlPrefix);

            testCodeFirstAdd(template, cseUrlPrefix);
            testCodeFirstAddString(template, cseUrlPrefix);
            testCodeFirstIsTrue(template, cseUrlPrefix);
            testCodeFirstSayHi2(template, cseUrlPrefix);
            testCodeFirstSayHi(template, cseUrlPrefix);
            testCodeFirstSaySomething(template, cseUrlPrefix);
            //            testCodeFirstRawJsonString(template, cseUrlPrefix);
            testCodeFirstSayHello(template, cseUrlPrefix);
            testCodeFirstReduce(template, cseUrlPrefix);
        }
    }

    private void testCodeFirstUserMap(RestTemplate template, String cseUrlPrefix) {
        User user1 = new User();
        user1.setNames(new String[] {"u1", "u2"});

        User user2 = new User();
        user2.setNames(new String[] {"u3", "u4"});

        Map<String, User> userMap = new HashMap<>();
        userMap.put("u1", user1);
        userMap.put("u2", user2);

        @SuppressWarnings("unchecked")
        Map<String, User> result = template.postForObject(cseUrlPrefix + "testUserMap",
                userMap,
                Map.class);

        TestMgr.check("u1", result.get("u1").getNames()[0]);
        TestMgr.check("u2", result.get("u1").getNames()[1]);
        TestMgr.check("u3", result.get("u2").getNames()[0]);
        TestMgr.check("u4", result.get("u2").getNames()[1]);
    }

    private void testCodeFirstTextPlain(RestTemplate template, String cseUrlPrefix) {
        String body = "a=1";
        String result = template.postForObject(cseUrlPrefix + "textPlain",
                body,
                String.class);
        TestMgr.check(body, result);
    }

    private void testCodeFirstBytes(RestTemplate template, String cseUrlPrefix) {
        byte[] body = new byte[] {0, 1, 2};
        byte[] result = template.postForObject(cseUrlPrefix + "bytes",
                body,
                byte[].class);
        TestMgr.check(3, result.length);
        TestMgr.check(1, result[0]);
        TestMgr.check(1, result[1]);
        TestMgr.check(2, result[2]);
    }

    protected void checkStatusCode(String microserviceName, int expectStatusCode, HttpStatus httpStatus) {
        TestMgr.check(expectStatusCode, httpStatus.value());
    }

    private void testCseResponse(String targetMicroserviceName, RestTemplate template,
            String cseUrlPrefix) {
        String srcMicroserviceName =
            RegistryUtils.getMicroserviceManager().getDefaultMicroserviceForce().getServiceName();
        String context = String.format("{x-cse-src-microservice=%s}", srcMicroserviceName);

        ResponseEntity<User> responseEntity =
            template.exchange(cseUrlPrefix + "cseResponse", HttpMethod.GET, null, User.class);
        TestMgr.check("User [name=nameA, age=100, index=0]", responseEntity.getBody());
        TestMgr.check("h1v " + context, responseEntity.getHeaders().getFirst("h1"));
        TestMgr.check("h2v " + context, responseEntity.getHeaders().getFirst("h2"));
        checkStatusCode(targetMicroserviceName, 202, responseEntity.getStatusCode());
    }

    private void testCodeFirstAddDate(RestTemplate template, String cseUrlPrefix) {
        Map<String, Object> body = new HashMap<>();
        Date date = new Date();
        body.put("date", date);

        int seconds = 1;
        Date result = template.postForObject(cseUrlPrefix + "addDate?seconds={seconds}",
                body,
                Date.class,
                seconds);
        TestMgr.check(new Date(date.getTime() + seconds * 1000), result);
    }

    protected void testExtend(RestTemplate template, String cseUrlPrefix) {

    }

    protected void testCodeFirstAddString(RestTemplate template, String cseUrlPrefix) {
        ResponseEntity<String> responseEntity =
            template.exchange(cseUrlPrefix + "addstring?s=a&s=b",
                    HttpMethod.DELETE,
                    null,
                    String.class);
        TestMgr.check("ab", responseEntity.getBody());
    }

    protected void testCodeFirstIsTrue(RestTemplate template, String cseUrlPrefix) {
        boolean result = template.getForObject(cseUrlPrefix + "istrue", boolean.class);
        TestMgr.check(true, result);
    }

    protected void testCodeFirstSayHi2(RestTemplate template, String cseUrlPrefix) {
        ResponseEntity<String> responseEntity =
            template.exchange(cseUrlPrefix + "sayhi/{name}/v2", HttpMethod.PUT, null, String.class, "world");
        TestMgr.check("world sayhi 2", responseEntity.getBody());
    }

    protected void testCodeFirstSayHi(RestTemplate template, String cseUrlPrefix) {
        ResponseEntity<String> responseEntity =
            template.exchange(cseUrlPrefix + "sayhi/{name}", HttpMethod.PUT, null, String.class, "world");
        TestMgr.check(202, responseEntity.getStatusCode());
        TestMgr.check("world sayhi", responseEntity.getBody());
    }

    protected void testCodeFirstSaySomething(RestTemplate template, String cseUrlPrefix) {
        Person person = new Person();
        person.setName("person name");

        HttpHeaders headers = new HttpHeaders();
        headers.add("prefix", "prefix  prefix");

        HttpEntity<Person> requestEntity = new HttpEntity<>(person, headers);
        String result = template.postForObject(cseUrlPrefix + "saysomething", requestEntity, String.class);
        TestMgr.check("prefix  prefix person name", result);
    }

    protected void testCodeFirstSayHello(RestTemplate template, String cseUrlPrefix) {
        Map<String, String> persionFieldMap = new HashMap<>();
        persionFieldMap.put("name", "person name from map");
        Person result = template.postForObject(cseUrlPrefix + "sayhello", persionFieldMap, Person.class);
        TestMgr.check("hello person name from map", result);

        Person input = new Person();
        input.setName("person name from Object");
        result = template.postForObject(cseUrlPrefix + "sayhello", input, Person.class);
        TestMgr.check("hello person name from Object", result);
    }

    protected void testCodeFirstAdd(RestTemplate template, String cseUrlPrefix) {
        Map<String, String> params = new HashMap<>();
        params.put("a", "5");
        params.put("b", "3");
        int result =
            template.postForObject(cseUrlPrefix + "add", params, Integer.class);
        TestMgr.check(8, result);
    }

    protected void testCodeFirstReduce(RestTemplate template, String cseUrlPrefix) {
        Map<String, String> params = new HashMap<>();
        params.put("a", "5");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "b=3");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Integer> result =
            template.exchange(cseUrlPrefix + "reduce?a={a}", HttpMethod.GET, requestEntity, Integer.class, params);
        TestMgr.check(2, result.getBody());
    }

}
