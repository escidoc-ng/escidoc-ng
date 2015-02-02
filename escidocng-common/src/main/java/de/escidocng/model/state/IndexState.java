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

package de.escidocng.model.state;

/**
 * A DTO for wrapping the state of a escidocng Index implementation
 */
public class IndexState {

    long storeSize;

    int shardsSize;

    long numDocs;

    long maxDocs;

    long totalFlushTime;

    long totalMergeTime;

    long numDocsToMerge;

    long sizeToMerge;

    long totalRefreshTime;

    private String name;

    public int getShardsSize() {
        return shardsSize;
    }

    public void setShardsSize(int shardsSize) {
        this.shardsSize = shardsSize;
    }

    public long getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(long numDocs) {
        this.numDocs = numDocs;
    }

    public long getMaxDocs() {
        return maxDocs;
    }

    public void setMaxDocs(long maxDocs) {
        this.maxDocs = maxDocs;
    }

    public long getTotalFlushTime() {
        return totalFlushTime;
    }

    public void setTotalFlushTime(long totalFlushTime) {
        this.totalFlushTime = totalFlushTime;
    }

    public long getTotalMergeTime() {
        return totalMergeTime;
    }

    public void setTotalMergeTime(long totalMergeTime) {
        this.totalMergeTime = totalMergeTime;
    }

    public long getNumDocsToMerge() {
        return numDocsToMerge;
    }

    public void setNumDocsToMerge(long numDocsToMerge) {
        this.numDocsToMerge = numDocsToMerge;
    }

    public long getSizeToMerge() {
        return sizeToMerge;
    }

    public void setSizeToMerge(long sizeToMerge) {
        this.sizeToMerge = sizeToMerge;
    }

    public long getTotalRefreshTime() {
        return totalRefreshTime;
    }

    public void setTotalRefreshTime(long totalRefreshTime) {
        this.totalRefreshTime = totalRefreshTime;
    }

    public long getStoreSize() {

        return storeSize;
    }

    public void setStoreSize(long storeSize) {
        this.storeSize = storeSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
