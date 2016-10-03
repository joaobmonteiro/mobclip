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
package br.com.mobclip;

import java.util.List;
import java.util.Map;

import br.com.mobclip.model.Property;
import br.com.mobclip.model.PriceHistory;
import br.com.mobclip.model.Status;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class PropertyService {

    @Autowired
    private PropertyRepository repository;
    @Autowired
    private MongoOperations mongoOperations;

    public void atualizarTodas(List<Property> properties, Map<String, Property> propertyByLink) {
        for (Property property : properties) {
            Property existent = propertyByLink.get(property.getLink());
            if (existent != null) {
                PriceHistory h = new PriceHistory(existent);
                existent.addHistory(h);
                existent.updatePrice(property.getPrice());
                save(existent);
            } else {
                save(property);
            }
        }
    }

    public List<Property> listarTodas(Integer tipo) {
        return repository.findByType(tipo);
    }

    public List<Property> findAllWithoutBuildingArea(Integer type) {
        Query query = new Query();
        query.addCriteria(where("status").is(Status.NEW)
                .and("totalArea").is(null)
                .and("buildingArea").is(null))
            .fields()
                .include("_id")
                .include("link");

        return mongoOperations.find(query, Property.class);
    }

    public List<Property> saveAll(List<Property> properties) {
        return repository.save(properties);
    }

    public Property save(Property property) {
        return repository.save(property);
    }

    public List<Property> findAllForSelling(Integer type) {
        Query query = new Query();
        query.addCriteria(where("status").ne(Status.SOLD)
                .and("type").is(type));
        
        return mongoOperations.find(query, Property.class);
    }
}