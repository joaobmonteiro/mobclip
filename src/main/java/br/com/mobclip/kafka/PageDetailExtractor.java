/*
 * Copyright 2016 Pivotal Software, Inc..
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

import static br.com.mobclip.kafka.Topics.PAGE_DETAIL_EXTRACTOR;
import static br.com.mobclip.kafka.Topics.SAVE_PROPERTY;
import br.com.mobclip.model.Property;
import static br.com.mobclip.util.Constants.POLL_DELAY;
import static br.com.mobclip.util.Constants.TIMEOUT;
import static br.com.mobclip.util.Constants.USER_AGENT;
import java.sql.Savepoint;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PageDetailExtractor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PageDetailExtractor.class.getName());
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("pt", "BR"));

    @Override
    public void run() {

        Consumer<String, Property> consumer = ConsumerFactory.getPropertyConsumer();
        consumer.subscribe(Arrays.asList(PAGE_DETAIL_EXTRACTOR));

        try {
            while (true) {
                ConsumerRecords<String, Property> records = consumer.poll(POLL_DELAY);
                LOGGER.log(Level.INFO, "records fetched {0}", records.count());

                Producer<String, Property> propertyProducer = ProducerFactory.getPropertyProducer();
                
                for (ConsumerRecord<String, Property> record : records) {

                    Property property = record.value();
                    try {
                        LOGGER.log(Level.INFO, "extracting details from property #{0}", property.getLink());

                        extractDetails(property);
                        propertyProducer.send(new ProducerRecord<>(SAVE_PROPERTY, property));

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "{0} id {1} {2}", new Object[]{property.getLink(), property.getId(), e.getMessage()});
                    }
                }

                consumer.commitAsync((Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) -> {
                    if (exception != null) {
                        LOGGER.log(Level.SEVERE, null, exception);
                    } else {
                        offsets.entrySet().stream()
                                .forEach(entry -> LOGGER.log(Level.INFO, "records commited: partition {0}, offset {1}",
                                        Arrays.asList(entry.getKey().partition(), entry.getValue().offset())));
                    }
                });

            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            consumer.close();
        }
    }

    private Property extractDetails(final Property property) throws Exception {
        Document doc = Jsoup.connect(property.getLink())
                .timeout(TIMEOUT)
                .userAgent(USER_AGENT)
                .get();

        Elements elements = doc.select("td + :containsOwn(m²)");

        for (Element element : elements) {
            Element parentRow = element.parents().first();
            String column = parentRow.child(0).text();
            String value = parentRow.child(1).text();
            Double area = parseArea(value);

            if (column != null && column.contains("Área construída")) {
                property.setBuildingArea(area);
            } else if (column != null && column.contains("Área total")) {
                property.setTotalArea(area);
            }
        }

        //landlord
        Element element = doc.select(".dados").first();
        property.setLandlord(element.text());

        return property;
    }

    private Double parseArea(String area) {
        try {
            return numberFormat.parse(area.substring(0, area.length() - 3)).doubleValue();
        } catch (Exception e) {
            return null;
        }
    }
}
