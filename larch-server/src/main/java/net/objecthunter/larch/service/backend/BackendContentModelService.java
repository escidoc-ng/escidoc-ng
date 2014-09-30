
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

    /**
     * Create a new ContentModel
     * 
     * @param c
     * @return contentModelId
     * @throws IOException
     */
    String create(ContentModel c) throws IOException;

    /**
     * Delete ContentModel.
     * Check if unused, otherwise throw Exception.
     * 
     * @param id
     * @throws IOException
     */
    void delete(String id) throws IOException;

    /**
     * Check if ContentModel with given id already exists.
     * 
     * @param id
     * @return
     * @throws IOException
     */
    boolean exists(String id) throws IOException;


}
