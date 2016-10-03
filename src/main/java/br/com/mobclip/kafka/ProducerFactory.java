/* 
 * Copyright 2016 João Bosco Monteiro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.mobclip.kafka;

import br.com.mobclip.model.Property;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerFactory {

    private static Producer<String, String> producer;
    private static Producer<String, Property> propertyProducer;

    public static Producer<String, String> getPageProducer() {

        if (producer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", StringSerializer.class.getName());
            producer = new KafkaProducer<>(props);
        }

        return producer;
    }
    
    public static Producer<String, Property> getPropertyProducer() {

        if (propertyProducer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", StringSerializer.class.getName());
            props.put("value.serializer", PropertySerializer.class.getName());
            propertyProducer = new KafkaProducer<>(props);
        }

        return propertyProducer;
    }
}
