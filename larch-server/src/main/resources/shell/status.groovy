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
package shell

import net.objecthunter.larch.model.Describe
import net.objecthunter.larch.model.state.LarchState
import net.objecthunter.larch.service.RepositoryService
import net.objecthunter.larch.util.ServiceProvider
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.crsh.text.Color
import org.crsh.text.Decoration
import org.crsh.text.RenderPrintWriter

class status {

    RenderPrintWriter sink;

    @Usage("Repository status information")
    @Command
    def main(InvocationContext ctx) {
        final RepositoryService repositoryService = (RepositoryService) ServiceProvider.
                getService(ctx,
                           RepositoryService.class);
        final Describe desc = repositoryService.describe();
        final LarchState state = repositoryService.status();
        final StringBuilder resp = new StringBuilder();
        sink = ctx.getWriter();
        printHeader("Module Larch");
        printField("Version\t\t\t\t");
        printValue(desc.larchVersion);
        printField("Cluster name\t\t\t");
        printValue(desc.larchClusterName);
        printField("Host name\t\t\t");
        printValue(desc.larchHost);
        printHeader("Module ElasticSearch");
        printField("Version\t\t\t\t");
        printValue(desc.esVersion);
        printField("Master node name\t\t");
        printValue(desc.esMasterNodeName);
        printField("Master node address\t\t");
        printValue(desc.esMasterNodeAddress);
        printField("Node name\t\t\t");
        printValue(desc.esNodeName);
        printField("Number of nodes\t\t\t");
        printValue(String.valueOf(desc.esNumDataNodes));
        printField("Number of indexed records\t");
        printValue(String.valueOf(desc.esNumIndexedRecords));
        printField("Index name\t\t\t");
        printValue(state.indexState.name);
        printField("Number of docs\t\t\t");
        printValue(String.valueOf(state.indexState.numDocs));
        printField("Max number of docs\t\t");
        printValue(String.valueOf(state.indexState.maxDocs));
        printField("Number of docs to merge\t\t");
        printValue(String.valueOf(state.indexState.numDocsToMerge));
        printField("Store size\t\t\t");
        printValue(String.valueOf(state.indexState.storeSize));
        printField("Shards size\t\t\t");
        printValue(String.valueOf(state.indexState.shardsSize));
        printField("Size to merge\t\t\t");
        printValue(String.valueOf(state.indexState.sizeToMerge));
        printField("Total flush time\t\t");
        printValue(String.valueOf(state.indexState.totalFlushTime));
        printField("Total refresh time\t\t");
        printValue(String.valueOf(state.indexState.totalRefreshTime));
        printHeader("Module Blobstore");
        printField("Name\t\t\t\t");
        printValue(state.blobstoreState.name);
    }

    def printHeader(String header) {
        sink.print("\n");
        sink.println(header, Decoration.bold, Color.green);
    }

    def printField(String field) {
        sink.print(field)
    }

    def printValue(String value) {
        sink.println(value, Color.yellow);
    }
}