package com.rsicms.exportgenerator.api;

import com.rsicms.exportgenerator.GenerationParameters;

/**
 * Objects that generate RSuite browse trees
 * as exported from RSuite 3.6.
 */
public interface BrowseTreeGenerator {

    /**
     * Generate a browse tree as exported.
     * @throws Exception
     */
    public void generateBrowseTree() throws Exception;
}
