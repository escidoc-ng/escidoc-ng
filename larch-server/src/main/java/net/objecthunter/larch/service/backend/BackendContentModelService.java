
package net.objecthunter.larch.service.backend;

import java.io.IOException;

import net.objecthunter.larch.model.ContentModel;


/**
 * Service definition for content-model storage
 */
public interface BackendContentModelService {

    /**
     * retrieve ContentModel.
     * 
     * @param contentModelId
     * @return
     * @throws IOException
     */
    ContentModel retrieve(String contentModelId) throws IOException;


}
