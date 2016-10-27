package com.rsicms.exportgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by ekimber on 10/27/16.
 */
public class GenerationParameters extends Properties {

    long maxXmlMOs = -1;
    long maxBinaryMOs = -1;

    public GenerationParameters(File generationParameters) throws Exception {
        this.load(new FileInputStream(generationParameters));

    }

    public String getOutdir() {
        return (String)this.get("outdir");
    }

    public long getMaxMoCount() {
        return this.getMaxXmlMOs() + this.getMaxBinaryMOs();
    }

    public long getMaxXmlMOs() {
        if (this.maxXmlMOs == -1) {
            String temp = (String)get("maxXmlMOs");
            if (temp == null || "".equals(temp.trim())) {
                temp = "10000";
            }
            maxXmlMOs = Integer.parseInt(temp);
        }
        return maxXmlMOs;
    }

    public long getMaxBinaryMOs() {
        if (this.maxBinaryMOs == -1) {
            String temp = (String)get("maxBinaryMOs");
            if (temp == null || "".equals(temp.trim())) {
                temp = "10000";
            }
            maxBinaryMOs = Integer.parseInt(temp);
        }
        return maxBinaryMOs;
    }
}
