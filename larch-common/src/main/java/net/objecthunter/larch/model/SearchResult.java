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

package net.objecthunter.larch.model;

import java.util.List;

public class SearchResult {

    private long totalHits;

    private int hits;

    private int numRecords;

    private int maxRecords;

    private int offset;

    private long duration;

    private String scrollId;

    private long prevOffset;

    private long nextOffset;

    private String term;

    private List<?> data;

    /**
     * Get the total hit number
     * 
     * @return the total number of hits
     */
    public long getTotalHits() {
        return totalHits;
    }

    /**
     * Set the totalnumber of hits
     * 
     * @param totalHits the number of hits to set
     */
    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    /**
     * Get the number of hits in this result
     * 
     * @return the number of hits
     */
    public int getHits() {
        return hits;
    }

    /**
     * Set the number of hits in this result
     * 
     * @param hits the number of hits to set
     */
    public void setHits(int hits) {
        this.hits = hits;
    }

    /**
     * Get the number of records requested by the user
     * 
     * @return the number of records
     */
    public int getNumRecords() {
        return numRecords;
    }

    /**
     * Set the number of records requested by the user
     * 
     * @param numRecords the number of records to set
     */
    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    /**
     * Get the maximum number of records returned in a search result
     * 
     * @return the maximum number of records
     */
    public int getMaxRecords() {
        return maxRecords;
    }

    /**
     * Set the maximum number of records returned
     * 
     * @param maxRecords the maximum number to set
     */
    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    /**
     * Get the offset for this search result
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set the offset for this search result
     * 
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Get the duration of the search duration
     * 
     * @return the duration of the search
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set the duration of the search
     * 
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Get the scroll id for this search result
     * 
     * @return the scroll id
     */
    public String getScrollId() {
        return scrollId;
    }

    /**
     * Set the scroll id for this search result
     * 
     * @param scrollId the scroll id to set
     */
    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    /**
     * Get the previous offset for this search result
     * 
     * @return the previuous offset
     */
    public long getPrevOffset() {
        return prevOffset;
    }

    /**
     * Set the previous offset for this search result
     * 
     * @param prevOffset the previous offset to set
     */
    public void setPrevOffset(long prevOffset) {
        this.prevOffset = prevOffset;
    }

    /**
     * Get the next offset for this search result
     * 
     * @return the next offset
     */
    public long getNextOffset() {
        return nextOffset;
    }

    /**
     * Set the next offset of the search result
     * 
     * @param nextOffset the next offset to set
     */
    public void setNextOffset(long nextOffset) {
        this.nextOffset = nextOffset;
    }

    /**
     * Get the search terms
     * 
     * @return the terms
     */
    public String getTerm() {
        return term;
    }

    /**
     * Set hte search term
     * 
     * @param term the search term to set
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Get the result set of entities found
     * 
     * @return the list of entities found and contained in this search result
     */
    public List<?> getData() {
        return data;
    }

    /**
     * set the entities contained in this search result
     * 
     * @param data the entities to set
     */
    public void setData(List<?> data) {
        this.data = data;
    }
}
