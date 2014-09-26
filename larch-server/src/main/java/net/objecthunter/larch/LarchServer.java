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

package net.objecthunter.larch;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Main class for running a Larch server instance from the command line
 */
public class LarchServer {

    /**
     * The main entry point of the application when started from the Command line
     * 
     * @param args the args passed on the command line
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .showBanner(false)
                .web(true)
                .sources(LarchServerConfiguration.class, LarchServerSecurityConfiguration.class,
                        OAuth2ServerConfiguration.class)
                .run(args);
    }
}
