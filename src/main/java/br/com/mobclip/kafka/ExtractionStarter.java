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

import static br.com.mobclip.kafka.Topics.PAGE_EXTRACTOR;
import static br.com.mobclip.util.Constants.TIMEOUT;
import static br.com.mobclip.util.Constants.URL;
import static br.com.mobclip.util.Constants.USER_AGENT;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ExtractionStarter implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(ExtractionStarter.class.getName());
    private int goal;
    private int type;

    public void configure(int goal, int type) {
        this.goal = goal;
        this.type = type;
    }

    @Override
    public void run() {

        try (Producer<String, String> producer = ProducerFactory.getPageProducer()) {
            String url = String.format(URL, goal, type);
            Document doc = Jsoup.connect(url + 1).timeout(TIMEOUT)
                    .userAgent(USER_AGENT)
                    .get();

            Element element = doc.getElementsByAttributeValue("name", "pagina")
                    .first().getAllElements().last();
            
            int pages = Integer.valueOf(element.text());

            LOGGER.log(Level.INFO, "{0} pages found", pages);

            for (int i = 1; i <= pages; i++) {
                producer.send(new ProducerRecord<>(PAGE_EXTRACTOR, "url", url + i));
            }
            LOGGER.info("all pages submitted to kafka topics");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}
