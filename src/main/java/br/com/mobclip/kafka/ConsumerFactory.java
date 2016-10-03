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
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumerFactory {

    public static Consumer<String, Property> getPropertyConsumer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "fleet-1");
        props.put("max.poll.records","5");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", PropertyDeserializer.class.getName());

        Consumer<String, Property> consumer = new KafkaConsumer<>(props);
        return consumer;
    }
    
    public static Consumer<String, String> getPageConsumer(){
        Properties props = new Properties();
        props.put("auto.commit.offset","false");
        props.put("max.poll.records","5");
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "fleet-1");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }
}
