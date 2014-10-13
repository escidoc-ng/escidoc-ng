/**
 * 
 */
package net.objecthunter.larch.service;

import java.io.IOException;

import net.objecthunter.larch.model.Entity;


/**
 * @author mih
 *
 */
public interface EntityValidatorService {
    
    /**
     * validate Entity against ContentModel.
     * 
     * @param entity
     * @throws IOException
     */
    void validate(Entity entity) throws IOException;
}
