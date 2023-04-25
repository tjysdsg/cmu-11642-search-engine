/**
 * Copyright (c) 2023, Carnegie Mellon University.  All Rights Reserved.
 */
package search_engine;

/**
 *  An object that stores parameters for the unranked Boolean
 *  retrieval model (there are none) and indicates to the query
 *  operators how the query should be evaluated.
 */
public class RetrievalModelUnrankedBoolean extends RetrievalModel {

    public String defaultQrySopName() {
        return new String("#and");
    }

}
