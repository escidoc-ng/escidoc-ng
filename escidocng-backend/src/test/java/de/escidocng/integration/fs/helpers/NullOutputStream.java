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


package de.escidocng.integration.fs.helpers;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ruckus on 30.06.14.
 */
public class NullOutputStream extends OutputStream {

    private static NullOutputStream INSTANCE = new NullOutputStream();

    private NullOutputStream() {
        super();
    }

    public static NullOutputStream getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(int b) throws IOException {
        // do nothing;
    }
}