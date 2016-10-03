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

import br.com.mobclip.PropertyService;
import br.com.mobclip.model.Property;
import br.com.mobclip.util.Constants;
import static br.com.mobclip.util.Constants.POLL_DELAY;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(scopeName = "prototype")
public class PropertyPersister implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PropertyPersister.class.getName());

    @Autowired
    private PropertyService propertyService;

    @Override
    public void run() {
        Consumer<String, Property> consumer = ConsumerFactory.getPropertyConsumer();
        consumer.subscribe(Arrays.asList(Topics.SAVE_PROPERTY));

        try {
            while (true) {
                ConsumerRecords<String, Property> records = consumer.poll(POLL_DELAY);
                LOGGER.log(Level.INFO, "records fetched to persist {0}", records.count());
                for (ConsumerRecord<String, Property> record : records) {
                    Property property = record.value();
                    propertyService.save(property);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            consumer.close();
        }
    }
}
