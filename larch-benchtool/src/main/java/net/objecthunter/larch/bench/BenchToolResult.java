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

package net.objecthunter.larch.bench;

public class BenchToolResult {

    private final long duration;

    private final long size;

    public BenchToolResult(long size, long duration) {
        this.duration = duration;
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public float getThroughput() {
        return ((float) size * 1000f) / ((float) duration * 1024f * 1024f);
    }

    public long getDuration() {
        return duration;
    }
}
