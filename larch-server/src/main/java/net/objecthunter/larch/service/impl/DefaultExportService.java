/* 
 * Copyright 2014 FIZ Karlsruhe
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

package net.objecthunter.larch.service.impl;

import static net.objecthunter.larch.util.FileSystemUtil.checkAndCreate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.ExportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of a {@link net.objecthunter.larch.service.ExportService} which is able to export JSON data
 * to the File system
 */
public class DefaultExportService implements ExportService {

    private File directory;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() throws IOException {
        this.directory = new File(env.getProperty("escidocng.export.path"));
        checkAndCreate(this.directory);
    }

    @Override
    public void export(Entity e) throws IOException {
        final File f = new File(directory, e.getId() + ".json");
        try (final OutputStream sink = new FileOutputStream(f)) {
            mapper.writeValue(sink, e);
        }
    }
}
