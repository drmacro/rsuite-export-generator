package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.GenerationParameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Output generator that generates DITA files (maps, topics, etc.).
 */
public class DitaManagedObjectGenerator extends ManagedObjectGeneratorBase {

    private static Log log = LogFactory.getLog(DitaManagedObjectGenerator.class);

    public DitaManagedObjectGenerator(File outdir, GenerationParameters generationParameters) {
        super(outdir, generationParameters);
    }

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

        File contentDir = new File(outdir, "rsuite.content");
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
            int moid = nextMoId();
            File moDir = new File(childDir, "" + moid + "");
            if (!moDir.mkdirs()) {
                throw new RuntimeException("Failed to create directory \"" + moDir.getAbsolutePath() + "\"");
            }

            makeManagedObject(moDir, moid);

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

    public void makeManagedObject(File moDir, int moid) throws Exception {
        File contentXml = new File(moDir, "content.xml");
        String title = getRandomWords(2,5);
        makeTopic(moid, contentXml, title);
        ArrayList<String> versionSpecs = new ArrayList<String>();
        int majorVer = 1;
        int minorVer = 0;
        int versionCount = ThreadLocalRandom.current().nextInt(0, generationParameters.getMaxVersions() + 1);
        for (int i = 0; i < versionCount; i++) {
            if (i > 0 && i % 3 == 0) {
                majorVer++;
                minorVer = 0;
            }
            String versionSpec = majorVer + "." + minorVer;
            minorVer++;
            versionSpecs.add(versionSpec);
            File verFile = new File(moDir, moid + "-" + versionSpec + ".xml");
            makeTopic(moid, verFile, title);
        }
        makeResourceFileForXmlMo(moDir, moid, title, versionSpecs);
    }


    protected void makeTopic(int moid, File resultFile, String title)
            throws Exception {
        FileOutputStream fos = new FileOutputStream(resultFile);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new BufferedOutputStream(
                        fos), "UTF-8");
        try {
            writer.writeStartDocument();
            writer.writeStartElement("topic");
            writer.writeNamespace("r", "http://www.rsuitecms.com/rsuite/ns/metadata");
            writer.writeNamespace("ditaarch", "http://dita.oasis-open.org/architecture/2005/");
            writer.writeAttribute("id", "topic-" + moid);
            writer.writeAttribute("class", "- topic/topic ");
            writer.writeAttribute("r", "http://www.rsuitecms.com/rsuite/ns/metadata", "rsuiteId", "" + moid);
            writer.writeAttribute("ditaarch:DITAArchVersion","1.2");
            writer.writeAttribute("domains", "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)");
            // Title
            writer.writeStartElement("title");
            writer.writeAttribute("class", "- topic/title ");
            writer.writeCharacters(title);
            writer.writeEndElement();
            // Body
            writer.writeStartElement("body");
            writer.writeAttribute("class", "- topic/body ");
            makeParagraphs(writer);
            writer.writeEndElement();
            // End topic
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (Exception e) {
            log.error("makeTopic(): " + e.getClass().getSimpleName() + " Writing topic file: " + e.getMessage());
            throw e;
        } finally {
            writer.flush();
            writer.close();
            fos.close();
        }

    }

    protected void makeParagraphs(XMLStreamWriter writer) throws XMLStreamException {
        int paraCnt = ThreadLocalRandom.current().nextInt(1, 10);
        for (int i = 0; i < paraCnt; i++) {
            writer.writeStartElement("p");
            writer.writeAttribute("class", "- topic/p ");
            writer.writeCharacters(getRandomWords(7, 30));
            writer.writeEndElement();
        }
    }


}
