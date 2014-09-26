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

package net.objecthunter.larch.helpers;

import java.util.Arrays;
import java.util.List;

import net.objecthunter.larch.model.MetadataType;

public abstract class MetadataTypes {

    private static List<MetadataType> defaultTypes = Arrays.asList(
            new MetadataType("DC", "http://dublincore.org/schemas/xmls/qdc/2008/02/11/dc.xsd"),
            new MetadataType("SIMPLEDC", "http://dublincore.org/schemas/xmls/qdc/2008/02/11/simpledc.xsd"),
            new MetadataType("DCTERMS", "http://dublincore.org/schemas/xmls/qdc/2008/02/11/dcterms.xsd"),
            new MetadataType("MARC21SLIM", "http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd"),
            new MetadataType("PREMIS1.0-CONTAINER", "http://www.loc.gov/standards/premis/PREMIS-v1-0.xsd"),
            new MetadataType("PREMIS1.0-RIGHTS", "http://www.loc.gov/standards/premis/Rights-v1-0.xsd"),
            new MetadataType("PREMIS1.0-OBJECT", "http://www.loc.gov/standards/premis/Object-v1-0.xsd"),
            new MetadataType("PREMIS1.0-AGENT", "http://www.loc.gov/standards/premis/Agent-v1-0.xsd"),
            new MetadataType("PREMIS1.0-EVENT", "http://www.loc.gov/standards/premis/Event-v1-0.xsd"),
            new MetadataType("PREMIS1.1-CONTAINER", "http://www.loc.gov/standards/premis/v1/PREMIS-v1-1.xsd"),
            new MetadataType("PREMIS1.1-RIGHTS", "http://www.loc.gov/standards/premis/v1/Rights-v1-1.xsd"),
            new MetadataType("PREMIS1.1-OBJECT", "http://www.loc.gov/standards/premis/v1/Object-v1-1.xsd"),
            new MetadataType("PREMIS1.1-AGENT", "http://www.loc.gov/standards/premis/v1/Agent-v1-1.xsd"),
            new MetadataType("PREMIS1.1-EVENT", "http://www.loc.gov/standards/premis/v1/Event-v1-1.xsd"),
            new MetadataType("PREMIS2.0", "http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd"),
            new MetadataType("PREMIS2.1", "http://www.loc.gov/standards/premis/v2/premis-v2-1.xsd"),
            new MetadataType("PREMIS2.2", "http://www.loc.gov/standards/premis/premis.xsd")
            );

    public static List<MetadataType> getDefaultMetadataTypes() {
        return defaultTypes;
    }
}
