package shell

import net.objecthunter.larch.model.Entity
import net.objecthunter.larch.model.SearchResult
import net.objecthunter.larch.util.ServiceProvider
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.crsh.text.Color
import org.crsh.text.RenderPrintWriter
import org.elasticsearch.search.SearchService

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
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/

class ls {

    @Usage("List entities")
    @Command
    def main(InvocationContext ctx) {
        final SearchService searchService = ServiceProvider.getService(ctx, SearchService.class);
        final SearchResult result = searchService.scanEntities(0);
        final RenderPrintWriter sink = ctx.getWriter();
        sink.print("Number of entites\t\t");
        sink.println(result.getHits(), Color.yellow);
        sink.print("Duration\t\t\t");
        sink.println(result.getDuration() + " ms\n", Color.yellow);
        if (result.getHits() == 0) {
            sink.println("  No results")
            return;
        }
        sink.print("+----------------------------------+");
        sink.println("----------------------------------+");
        sink.format("| %32s |", "Id");
        sink.format(" %32s |\n", "Label");
        sink.print("+----------------------------------+");
        sink.println("----------------------------------+");
        for (Entity e : result.getData()) {
            sink.format("| %32s |", e.getId());
            sink.format(" %32s |\n", e.getLabel());
        }
        sink.print("+----------------------------------+");
        sink.println("----------------------------------+");
    }
}
