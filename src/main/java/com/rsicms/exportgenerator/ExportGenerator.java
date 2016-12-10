package com.rsicms.exportgenerator;

import com.rsicms.exportgenerator.api.BrowseTreeGenerator;
import com.rsicms.exportgenerator.api.ManagedObjectGenerator;
import com.rsicms.exportgenerator.api.MoType;
import com.rsicms.exportgenerator.generation.DefaultBrowseTreeGenerator;
import com.rsicms.exportgenerator.generation.DitaManagedObjectGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.file.FileSystems;

/**
 * Generates a fake export data set in order to then do scale
 * and performance testing of RSuite's 3.6->3.7+ importer.
 *
 */
public class ExportGenerator
{
    private final GenerationParameters generationParameters;

    private static Log log = LogFactory.getLog(ExportGenerator.class);
    private final File outdir;


    public ExportGenerator(File parametersFile) throws Exception {
        this.generationParameters = new GenerationParameters(parametersFile);

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
        generationParameters.setOutputDirectory(outdir);
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

        ManagedObjectGenerator moGenerator = new DitaManagedObjectGenerator(generationParameters);

        moGenerator.generateManagedObjects();
        BrowseTreeGenerator browseGenerator = new DefaultBrowseTreeGenerator(generationParameters);
        browseGenerator.generateBrowseTree();

        // Now write the ids.xml file:

        writeIdsXml(generationParameters);

        System.out.println("Generation summary:");
        System.out.println("      XML MOs: " + generationParameters.getManagedObjectsOfType(MoType.XML).size());
        System.out.println("  Non-XML MOs: " + generationParameters.getManagedObjectsOfType(MoType.NONXML).size());
        System.out.println("          CAs: " + generationParameters.getManagedObjectsOfType(MoType.CA).size());
        System.out.println("       MORefs: " + generationParameters.getManagedObjectsOfType(MoType.MOREF).size());


        log.info("Export generation done.");
    }

    private void writeIdsXml(GenerationParameters generationParameters) throws Exception {
        File resultFile = new File(generationParameters.getOutputDirectory(), "ids.xml");
        FileOutputStream fos = new FileOutputStream(resultFile);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new BufferedOutputStream(
                        fos), "UTF-8");
        try {
            writer.writeStartDocument();
            writer.writeStartElement("ids");
            writer.writeCharacters("" + generationParameters.getNextMoId());
            writer.writeEndElement();
        } catch (Exception e) {
            log.error("makeResourceFileForXmlMo(): " + e.getClass().getSimpleName() + " Writing ids.xml file: " + e.getMessage());
            throw e;
        } finally {
            writer.flush();
            writer.close();
            fos.close();
        }
    }

    public GenerationParameters getGenerationParameters() {
        return this.generationParameters;
    }

}
