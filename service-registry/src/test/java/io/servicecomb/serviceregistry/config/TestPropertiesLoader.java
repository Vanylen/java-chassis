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

package io.servicecomb.serviceregistry.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.config.archaius.sources.ConfigModel;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceManager;
import io.servicecomb.serviceregistry.definition.DefinitionConst;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;

public class TestPropertiesLoader {
    private static MicroserviceManager microserviceManager = new MicroserviceManager();

    @BeforeClass
    public static void init() {
        MicroserviceConfigLoader loader = new MicroserviceConfigLoader();
        loader.loadAndSort();

        microserviceManager.init(loader);
    }

    @Test
    public void testMergeStrings() {
        Assert.assertEquals("abc123efg", AbstractPropertiesLoader.mergeStrings("abc", "123", "efg"));
    }

    @Test
    public void testEmptyExtendedClass() {
        Microservice microservice = microserviceManager.addMicroservice("default", "emptyExtendedClass");
        Assert.assertEquals(0, microservice.getProperties().size());
    }

    @Test
    public void testInvalidExtendedClass() {
        ConfigModel configModel = MicroserviceDefinition.createConfigModel("default", "invalidExtendedClass");
        @SuppressWarnings("unchecked")
        Map<String, Object> desc =
            (Map<String, Object>) configModel.getConfig().get(DefinitionConst.serviceDescriptionKey);
        desc.put("propertyExtentedClass", "invalidClass");
        MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(Arrays.asList(configModel));
        try {
            microserviceManager.addMicroservice(microserviceDefinition);
            Assert.fail("Must throw exception");
        } catch (Error e) {
            Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            Assert.assertEquals("invalidClass", e.getCause().getMessage());
        }
    }

    @Test
    public void testCanNotAssignExtendedClass() {
        ConfigModel configModel = MicroserviceDefinition.createConfigModel("default", "invalidExtendedClass");
        @SuppressWarnings("unchecked")
        Map<String, Object> desc =
            (Map<String, Object>) configModel.getConfig().get(DefinitionConst.serviceDescriptionKey);
        desc.put("propertyExtentedClass", "java.lang.String");
        MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(Arrays.asList(configModel));
        try {
            microserviceManager.addMicroservice(microserviceDefinition);
            Assert.fail("Must throw exception");
        } catch (Error e) {
            Assert.assertEquals(
                    "Define propertyExtentedClass java.lang.String in yaml, but not implement the interface PropertyExtended.",
                    e.getMessage());
        }
    }

    @Test
    public void testMicroservicePropertiesLoader() throws Exception {
        Microservice microservice = microserviceManager.findMicroservice("default");
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key1", "value1");
        expectedMap.put("key2", "value2");
        expectedMap.put("ek0", "ev0");
        Assert.assertEquals(expectedMap, microservice.getProperties());
    }

    @Test
    public void testInstancePropertiesLoader() {
        Microservice microservice = microserviceManager.findMicroservice("default");
        MicroserviceInstance instance = microservice.getIntance();
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("key0", "value0");
        expectedMap.put("ek0", "ev0");
        Assert.assertEquals(expectedMap, instance.getProperties());
    }

}
