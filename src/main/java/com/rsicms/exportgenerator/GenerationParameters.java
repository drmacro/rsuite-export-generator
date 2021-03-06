package com.rsicms.exportgenerator;

import com.rsicms.exportgenerator.api.MoType;
import com.rsicms.exportgenerator.generation.ManagedObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by ekimber on 10/27/16.
 */
public class GenerationParameters extends Properties {

    private int moid = 1000; // Start with MO 1000 so MO IDs look realistic.
    private int progressCtr = 0;
    private int dotCtr = 0;

    long maxXmlMOs = -1;
    long maxBinaryMOs = -1;
    int maxVersions = -1;
    private Map<String, ManagedObject> mosById = new HashMap<String, ManagedObject>();
    private Map<MoType, ArrayList> mosByType = new HashMap<MoType, ArrayList>();
    private File outputDirectory;

    public GenerationParameters(File generationParameters) throws Exception {
        this.load(new FileInputStream(generationParameters));
        // Initialize each of the type arrays:

    }

    public int getNextMoId() {
        // Show progress as MO IDs are consumed.
        // This will have the effect of showing the
        // progress as things are constructed.
        progressCtr++;
        if (progressCtr % 100 == 0) {
            System.out.print(".");
            dotCtr++;
            if (dotCtr >= 60) {
                System.out.print("\n");
                dotCtr = 0;
            }
        }

        return moid++;
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

    public int getMaxVersions() {
        if (this.maxVersions == -1) {
            String temp = (String)get("maxVersions");
            if (temp == null || "".equals(temp.trim())) {
                temp = "3";
            }
            maxVersions = Integer.parseInt(temp);
        }
        return maxVersions;

    }

    public ManagedObject addMo(int moid, MoType moType, String displayName) {
        ManagedObject mo = new ManagedObject(moid, moType, displayName);

        this.mosById.put("" + moid, mo);
        ArrayList mosOfType = this.mosByType.get(moType);
        if (null == mosOfType) {
            mosOfType = new ArrayList<ManagedObject>();
            this.mosByType.put(moType, mosOfType);
        }
        mosOfType.add(mo);
        return mo;
    }

    public ManagedObject getManagedObject(int moid) {
        ManagedObject mo = this.mosById.get("" + moid);
        return mo;
    }

    public ArrayList<ManagedObject> getManagedObjectsOfType(MoType moType) {
        ArrayList<ManagedObject> resultArray = this.mosByType.get(moType);
        if (null == resultArray) {
            resultArray = new ArrayList<ManagedObject>();
            this.mosByType.put(moType, resultArray);
        }

        return resultArray;
    }

    /**
     * Set the output directory file. This will usually have
     * been created from the value in the properties file.
     * Note that this is different from the "outdir" property
     * in the configuration file.
     * @param outputDirectory
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getOutputDirectory() {
        return this.outputDirectory;
    }

    /**
     * Get the max number of containers to have at any point in the browse tree.
     * @return
     */
    public int getBrowseWidth() {
        String propStr = this.getProperty("browseWidth", "10");
        int propVal = Integer.parseInt(propStr);
        return propVal;
    }

    /**
     * Get the max depth of containers for any branch of the browse tree.
     * @return
     */
    public int getBrowseDepth() {
        String propStr = this.getProperty("browseDepth", "5");
        int propVal = Integer.parseInt(propStr);
        return propVal;
    }

    public int getMaxContainerChildren() {

        String propStr = this.getProperty("maxContainerChildren", "100");
        int propVal = Integer.parseInt(propStr);
        return propVal;
    }

    public String getUser() {
        return "fakeuser";
    }

    /**
     * Create a new managed object of the specified type and with the
     * specified display name.
     * @param moType
     * @param displayName
     * @return
     */
    public ManagedObject newMo(MoType moType, String displayName) {
        ManagedObject result = addMo(getNextMoId(), moType, displayName);
        return result;
    }

    public int getManagedObjectCount() {
        return this.mosById.size();
    }
}
