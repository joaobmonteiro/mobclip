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
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class PropertyDeserializer implements Deserializer<Property>{

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Property deserialize(String topic, byte[] data) {
        try {
            String json = new String(data, Charset.forName("UTF-8"));
            return Property.fromJson(json);
        } catch (Exception e) {
            throw new SerializationException("cannot parse json to Property", e);
        }
    }

    @Override
    public void close() {
    }
}
