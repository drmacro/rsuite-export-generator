package com.rsicms.exportgenerator.api;

import com.rsicms.exportgenerator.GenerationParameters;

/**
 * Objects that generate RSuite browse trees
 * as exported from RSuite 3.6.
 */
public interface BrowseTreeGenerator {

    /**
     * Generate a browse tree as exported.
     * @param generationParameter
     * @throws Exception
     */
    public void generateBrowseTree(GenerationParameters generationParameter) throws Exception;
}
