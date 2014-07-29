
package net.objecthunter.larch.service.backend;

import java.io.IOException;

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;

/**
 * Service definition for entity version storage
 */
public interface BackendVersionService {

    /**
     * Add an old version of an entity to the old version storage and return the path
     * 
     * @param e the old version to store
     * @return the path to where the old version can be found
     */
    void addOldVersion(Entity e) throws IOException;

    /**
     * Retrieve an old version of an entity from the version storage
     * 
     * @param id the id of the entity to retrieve
     * @param versionNumber the number of the version
     * @return the requested old version of an entity
     */
    Entity getOldVersion(String id, int versionNumber) throws IOException;

    /**
     * Retrieve all old versions of an entity from the version storage
     * 
     * @param id the id of the entity to retrieve
     * @return the requested old versions of the entity as Entities-Object
     */
    Entities getOldVersions(String id) throws IOException;

    /**
     * Delete all old versions of an entity from the version storage
     * 
     * @param id the id of the entity to delete the versions of
     * @throws IOException e
     */
    void deleteOldVersions(String id) throws IOException;

}
