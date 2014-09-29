/* 
 * Copyright 2014 Frank Asseg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.bench;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;

import org.apache.commons.lang3.RandomStringUtils;

public abstract class BenchToolEntities {

    public static Entity createRandomEmptyEntity(String level2Id) {
        final Entity e = new Entity();
        e.setParentId(level2Id);
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setContentModelId(FixedContentModel.DATA.getName());
        return e;
    }

}
