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

import static br.com.mobclip.kafka.Topics.PAGE_DETAIL_EXTRACTOR;
import static br.com.mobclip.kafka.Topics.PAGE_EXTRACTOR;
import static br.com.mobclip.kafka.Topics.SAVE_PROPERTY;
import br.com.mobclip.model.Property;
import br.com.mobclip.util.Constants;
import static br.com.mobclip.util.Constants.POLL_DELAY;
import static br.com.mobclip.util.Constants.TIMEOUT;
import static br.com.mobclip.util.Constants.USER_AGENT;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class PageExtractor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PageExtractor.class.getName());
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("pt", "BR"));

    @Override
    public void run() {

        Consumer<String, String> consumer = ConsumerFactory.getPageConsumer();
        consumer.subscribe(Arrays.asList(PAGE_EXTRACTOR));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(POLL_DELAY);
                LOGGER.log(Level.INFO, "records fetched {0}", records.count());

                Producer<String, Property> propertyProducer = ProducerFactory.getPropertyProducer();
                
                for (ConsumerRecord<String, String> record : records) {
                    String url = record.value();
                    int type = 2;

                    List<Property> properties = extractPageData(url, type);
                    for (Property property : properties) {
                        propertyProducer.send(new ProducerRecord<>(PAGE_DETAIL_EXTRACTOR, property));
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

    private List<Property> extractPageData(final String url, final int type) {
        LOGGER.log(Level.INFO, "processing {0}", url);
        Document doc;
        try {
            doc = Jsoup.connect(url).timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .get();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cannot connect to {0}", url);
            return Collections.emptyList();
        }

        List<Property> properties = new ArrayList<>();
        Elements items = doc.select(".listagem").first().children();
        for (Element item : items) {
            Property property = new Property();
            property.setType(type);

            Element block = item.select(".desc2").first();

            Optional<Element> neighbourhood = Optional.of(block.getElementsByTag("b").first());
            neighbourhood.ifPresent((e) -> property.setNeighborhood(e.text()));

            Element bairroLink = item.getElementsByTag("a").first();
            String link = bairroLink.attr("href");
            property.setLink(link);

            List<TextNode> nodes = block.children().first().textNodes();
            String endereco = null;
            if (nodes.size() > 1) {
                endereco = nodes.get(1).text();
            } else if (!nodes.isEmpty()) {
                endereco = nodes.get(0).text();
            }

            property.setAddress(endereco);

            Element preco = item.select(".valor").first();
            try {
                String precoStr = preco.text().substring(3);
                property.setPrice(numberFormat.parse(precoStr).doubleValue());
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "property without price -> {0}", preco.text());
            }

            properties.add(property);

        }
        return properties;
    }
}
