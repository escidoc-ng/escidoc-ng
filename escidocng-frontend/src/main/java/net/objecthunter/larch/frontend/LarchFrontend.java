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
package net.objecthunter.larch.frontend;

import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Main class for running a Larch client instance from the command line
 * @author Michael Hopp
 */
public class LarchFrontend {

    /**
     * The main entry point of the application when started from the Command line
     * 
     * @param args the args passed on the command line
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .showBanner(false)
                .web(true)
                .sources(LarchFrontendConfiguration.class, LarchFrontendSecurityConfiguration.class)
                .run(args);
    }
}
