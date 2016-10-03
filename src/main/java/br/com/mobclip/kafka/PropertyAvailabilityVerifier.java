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

import br.com.mobclip.PropertyService;
import br.com.mobclip.model.Property;
import static br.com.mobclip.model.Status.SOLD;
import br.com.mobclip.util.Constants;
import static br.com.mobclip.util.Constants.USER_AGENT;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

public class PropertyAvailabilityVerifier {
    private static final Logger LOGGER = Logger.getLogger(PropertyAvailabilityVerifier.class.getName());
    
    @Autowired
    private PropertyService propertyService;

    public void verificarCasasVendidas(Integer tipo) {
        List<Property> properties = propertyService.findAllForSelling(tipo);
        for (Property property : properties) {
            try {
                int code = Jsoup.connect(property.getLink())
                        .userAgent(USER_AGENT)
                        .followRedirects(false)
                        .execute().statusCode();
                
                if (code == 302) {
                    property.setStatus(SOLD);
                    propertyService.save(property);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "{0} id {1} {2}", new Object[]{property.getLink(), property.getId(), e.getMessage()});
            }
        }
    }
}
