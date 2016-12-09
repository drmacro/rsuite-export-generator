package com.rsicms.exportgenerator.generation;

import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.ManagedObjectGenerator;
import com.rsicms.exportgenerator.api.MoType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Base superclass for all managed object generators
 */
public abstract class ManagedObjectGeneratorBase implements ManagedObjectGenerator {

    private static Log log = LogFactory.getLog(ManagedObjectGeneratorBase.class);

    // FIXME: The initial MOID should probably bet part of the generation parameter as should the output directory.

    protected final GenerationParameters generationParameters;

    public ManagedObjectGeneratorBase(GenerationParameters generationParameters) {
        this.generationParameters = generationParameters;
    }


    protected File getRandomDir(int min, int max, File parent) {
        int dirNum = ThreadLocalRandom.current().nextInt(min, max+1);
        File dir = new File(parent, "" + dirNum + "");
        if (!dir.exists()) {
            // log.info("Making directory " + dir.getName());
            if (!dir.mkdirs()) {
                throw new RuntimeException("Failed to create directory \"" + dir.getAbsolutePath() + "\"");
            }
        }
        return dir;
    }

    /**
     * Generate the file that holds the content for an XML managed object.
     * @param moid
     * @param resultFile
     * @param title
     * @throws Exception
     */
    protected abstract void makeXmlContent(int moid, File resultFile, String title) throws Exception;

    public void generateManagedObjects() throws Exception {
        /*
         * The managed object structure is:
         *
         * rsuite.content/
         *    managed-objects/
         *      0/
         *        {n}/ (directories with numbers between 0 and 99, not necessary consecutive)
         *          {moid}/ (One or more, usually no more than 2)
         *            {moid}.resource  (system metadata for the MO)
         *            {moid}-{versionid}.xml (XML for a specific version of the MO)
         *            content.xml      (XML for the latest version)
         *      ...
         *      999/ (1000 directories number 1-999
         */

        /*
         * Since RSuite tries to spread the MOs evenly over the directories, doing
         * a breadth-first traversal of the directories, rather than a depth-first.
         */

        File contentDir = new File(generationParameters.getOutputDirectory(),
                "rsuite.content");
        File mosDir = new File(contentDir, "managed-objects");
        if (!mosDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory \"" + mosDir.getAbsolutePath() + "\"");
        }

        long progressCtr = 0;
        long dotCtr = 0;
        // First make the top 100 directories.
        for (long i = 0; i < generationParameters.getMaxMoCount(); i++) {
            File topDir = getRandomDir(0, 999, mosDir);
            File childDir = getRandomDir(0,100, topDir);
            int moid = generationParameters.getNextMoId();
            File moDir = new File(childDir, "" + moid + "");
            if (!moDir.mkdirs()) {
                throw new RuntimeException("Failed to create directory \"" + moDir.getAbsolutePath() + "\"");
            }

            makeManagedObject(moDir, moid, MoType.XML);

            progressCtr++;
            if (progressCtr % 100 == 0) {
                System.out.print(".");
                dotCtr++;
                if (dotCtr >= 60) {
                    System.out.print("\n");
                    dotCtr = 0;
                }
            }
        }
        System.out.print("\n");


    }

    protected void makeManagedObject(File moDir, int moid, MoType moType) throws Exception {
        File contentXml = new File(moDir, "content.xml");
        String title = GenerationHelper.getRandomWords(2,5);
        makeXmlContent(moid, contentXml, title);
        ManagedObject mo = this.generationParameters.addMo(moid, moType.XML, title);
        ArrayList<String> versionSpecs = getVersions(moDir, mo);
        GenerationHelper.makeResourceFileForXmlMo(moDir, mo, versionSpecs);

    }

    /**
     * Get a set of randomly-generated versions for a managed object
     * @param moDir
     * @param mo
     * @return The versions. There will always be at least one version
     * @throws Exception
     */
    public ArrayList<String> getVersions(File moDir, ManagedObject mo) throws Exception {
        ArrayList<String> versionSpecs = new ArrayList<String>();
        int majorVer = 1;
        int minorVer = 0;
        int versionCount = ThreadLocalRandom.current().nextInt(1, generationParameters.getMaxVersions() + 1);
        for (int i = 0; i < versionCount; i++) {
            if (i > 0 && i % 3 == 0) {
                majorVer++;
                minorVer = 0;
            }
            String versionSpec = majorVer + "." + minorVer;
            minorVer++;
            versionSpecs.add(versionSpec);
            File verFile = new File(moDir, mo.getID() + "-" + versionSpec + ".xml");
            makeXmlContent(mo.getID(), verFile, mo.getDisplayName());
        }
        return versionSpecs;
    }

}
