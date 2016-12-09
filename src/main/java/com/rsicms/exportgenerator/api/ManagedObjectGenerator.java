package com.rsicms.exportgenerator.api;

import java.io.File;
import java.util.ArrayList;

/**
 * Objects that generate managed objects
 * as exported.
 */
public interface ManagedObjectGenerator {

    /**
     * Generates a set of managed objects using the parameters
     * and options set on the generator.
     */
    void generateManagedObjects() throws Exception;

}
