package com.rsicms.exportgenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a fake export data set in order to then do scale
 * and performance testing of RSuite's 3.6->3.7+ importer.
 *
 */
public class ExportGenerator
{
    private final GenerationParameters generationParameters;

    static Log log = LogFactory.getLog(ExportGenerator.class);
    private final File outdir;
    int moid = 1000; // Start with MO 1000 so MO IDs look realistic.

    public ExportGenerator(File parametersFilepath) throws Exception {
        this.generationParameters = new GenerationParameters(parametersFilepath);

        String outdirPath = generationParameters.getOutdir();
        if (outdirPath == null || "".equals(outdirPath.trim())) {
            outdirPath = "export";
        }
        File outdir = new File(outdirPath);
        if (!outdir.isAbsolute()) {
            outdir = FileSystems.getDefault().getPath(outdirPath).toFile();
        }
        if (!outdir.exists() && !outdir.mkdirs()) {
            throw new RuntimeException("Failed to create output directory \"" + outdir.getAbsolutePath() + "\"");
        }
        this.outdir = outdir;
    }

    public static void main(String[] args )
    {
        if (args.length < 1) {
            System.out.println("Usage:\n\n   ExportGenerator {generation parameters file}");
            System.exit(1);
        }

        String inFilename = args[0];
        File inFile = FileSystems.getDefault().getPath(inFilename).toFile();
        if (!inFile.exists()) {
            System.out.println("Parameter file \"" + inFile.getAbsolutePath() + " does not exist.");
            System.exit(1);
        }
        if (!inFile.canRead()) {
            System.out.println("Parameter file \"" + inFile.getAbsolutePath() + " exists but cannot be read.");
            System.exit(1);
        }


        try {
            ExportGenerator app = new ExportGenerator(inFile);
            app.generateExport();
        } catch (Exception e) {
            System.err.println(e.getClass().getSimpleName() + " generating fake export: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Do the export generation.
     */
    public void generateExport() throws Exception {
        log.info("Starting export generation to directory \"" + outdir.getAbsolutePath() + "\"...");

        this.generateManagedObjects();

        log.info("Export generation done.");
    }

    private void generateManagedObjects() throws Exception {
        /**
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

        /**
         * Since RSuite tries to spread the MOs evenly over the directories, doing
         * a breadth-first traversal of the directories, rather than a depth-first.
         */

        File contentDir = new File(outdir, "rsuite.content");
        File mosDir = new File(contentDir, "managed-objects");
        if (!mosDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory \"" + mosDir.getAbsolutePath() + "\"");
        }

        long progressCtr = 0;
        // First make the top 100 directories.
        for (long i = 0; i < generationParameters.getMaxMoCount(); i++) {
            File topDir = getRandomDir(0, 999, mosDir);
            File childDir = getRandomDir(0,100, topDir);
            int moid = nextMoId();
            File moDir = new File(childDir, "" + moid + "");
            if (!moDir.mkdirs()) {
                throw new RuntimeException("Failed to create directory \"" + moDir.getAbsolutePath() + "\"");
            }

            makeManagedObject(moDir, moid);

            progressCtr++;
            if (progressCtr % 100 == 0) {
                log.info(progressCtr + " MOs generated");
            }
        }


    }

    private void makeManagedObject(File moDir, int moid) throws Exception {
        File contentXml = new File(moDir, "content.xml");
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new BufferedOutputStream(
                                        new FileOutputStream(contentXml)), "UTF-8")));

        writer.write("<topic id=\"id" + moid + "\">\n");
        writer.write("</topic>\n");
        writer.flush();
        writer.close();
    }

    private int nextMoId() {
        return moid++;
    }

    private File getRandomDir(int min, int max, File parent) {
        int dirNum = ThreadLocalRandom.current().nextInt(min, max+1);
        File dir = new File(parent, "" + dirNum + "");
        if (!dir.exists()) {
            log.info("Making directory " + dir.getName());
            if (!dir.mkdirs()) {
                throw new RuntimeException("Failed to create directory \"" + dir.getAbsolutePath() + "\"");
            }
            ;
        }
        return dir;
    }

}
