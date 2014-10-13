/**
 * 
 */

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.util.List;

import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.EntityValidatorService;
import net.objecthunter.larch.service.backend.BackendContentModelService;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service that validates entity against ContentModel.
 * 
 * @author mih
 */
public class DefaultEntityValidatorService implements EntityValidatorService {

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private BackendContentModelService backendContentModelService;

    @Override
    public void validate(Entity entity) throws IOException {
        validateHierarchy(entity);
    }

    /**
     * Validate if contentModelId is != null and if hierarchy is correct.
     * Retrieve ContentModel and read allowedParentContentModels to validate.
     * 
     * @param entity
     * @throws IOException
     */
    private void validateHierarchy(Entity entity) throws IOException {
        if (StringUtils.isBlank(entity.getContentModelId())) {
            throw new InvalidParameterException("contentModelId may not be null");
        }
        ContentModel contentModel = backendContentModelService.retrieve(entity.getContentModelId());
        List<String> allowedParentContentModels = contentModel.getAllowedParentContentModels();

        if (allowedParentContentModels != null && !allowedParentContentModels.isEmpty() &&
                StringUtils.isNotBlank(entity.getParentId())) {
            Entity parent = backendEntityService.retrieve(entity.getParentId());
            if (allowedParentContentModels.contains(parent.getContentModelId())) {
                return;
            }
        } else if ((allowedParentContentModels == null || allowedParentContentModels.isEmpty()) &&
                StringUtils.isBlank(entity.getParentId())) {
            return;
        }
        throw new InvalidParameterException("invalid entity");
    }
}
