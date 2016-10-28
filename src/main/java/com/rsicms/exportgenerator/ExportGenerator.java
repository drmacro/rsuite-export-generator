package com.rsicms.exportgenerator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

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
    private int moid = 1000; // Start with MO 1000 so MO IDs look realistic.

    private static ArrayList<String> wordList = new ArrayList<String>();

    static {
        String wordsFilePath = "/resources/data-files/words.txt";
        InputStream inStream = ExportGenerator.class.getResourceAsStream(wordsFilePath);
        if (inStream == null) {
            throw new RuntimeException("Failed to load resource " + wordsFilePath);
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#")) {
                    line = reader.readLine();
                    continue;
                }
                wordList.add(line.trim());
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected " + e.getClass().getSimpleName() +
                    " reading words file: " + e.getMessage(), e);
        }
    }

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

    private void makeManagedObject(File moDir, int moid) throws Exception {
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

    private void makeResourceFileForXmlMo(File moDir,
                                          int moid,
                                          String title,
                                          ArrayList<String> versionSpecs)
            throws Exception {
        String userName = "fakeexportuser";

        File resourceFile = new File(moDir, moid + ".resource");
        FileOutputStream fos = new FileOutputStream(resourceFile);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new BufferedOutputStream(
                        fos), "UTF-8");
        try {
            writer.writeStartDocument();
            writer.writeStartElement("contentResource");
            writer.writeStartElement("acl");
            writer.writeStartElement("role");
            writer.writeAttribute("name", "RSuiteAdministrator");
            writer.writeCharacters("admin");
            writer.writeEndElement();// role
            writer.writeStartElement("role");
            writer.writeAttribute("name", "RSuiteEditor");
            writer.writeCharacters("edit,copy,delete");
            writer.writeEndElement();// role
            writer.writeStartElement("role");
            writer.writeAttribute("name", "*");
            writer.writeCharacters("list,view,reuse");
            writer.writeEndElement();// role
            writer.writeEndElement();// acl

            writer.writeEmptyElement("aliases");

            writer.writeStartElement("systemMetadata");
            writer.writeStartElement("id");
            writer.writeCharacters("" + moid);
            writer.writeEndElement();// id
            writer.writeStartElement("username");
            writer.writeCharacters(userName);
            writer.writeEndElement();// username
            writer.writeEndElement(); // systemMetadata

            writer.writeStartElement("versions");
            String moTagName = "topid";
            for (String versionSpec : versionSpecs) {
                makeVersionEntry(writer, title, versionSpec, moTagName, userName);
            }
            writer.writeEndElement(); // versions

            writer.writeEndElement(); // contentResource
            writer.writeEndDocument();
        } catch (Exception e) {
            log.error("makeResourceFileForXmlMo(): " + e.getClass().getSimpleName() + " Writing resource file: " + e.getMessage());
            throw e;
        } finally {
            writer.flush();
            writer.close();
            fos.close();
        }

    }

    private void makeVersionEntry(XMLStreamWriter writer,
                                  String title,
                                  String versionSpec,
                                  String moTagName,
                                  String userName)
            throws XMLStreamException, FileNotFoundException {
        writer.writeStartElement("versionEntry");
        writer.writeStartElement("displayName");
        writer.writeCharacters(title);
        writer.writeEndElement();
        writer.writeStartElement("dtCommitted");
        // FIXME: Generate the date within some configured range.
        writer.writeCharacters("2010-12-16T20:05:41.000Z");
        writer.writeEndElement();
        writer.writeStartElement("entryType");
        writer.writeCharacters("2"); // FIXME: Need to find out what the entry types are so this is accurate.
        writer.writeEndElement();
        writer.writeStartElement("lmd");
        writer.writeCharacters(""); // FIXME: Generate some LMD randomly
        writer.writeEndElement();
        writer.writeStartElement("localName");
        writer.writeCharacters(moTagName);
        writer.writeEndElement();
        writer.writeStartElement("namespaceUri");
        writer.writeCharacters(""); // No namespace for DITA.
        writer.writeEndElement();
        writer.writeStartElement("note");
        writer.writeCharacters(getRandomWords(1, 4));
        writer.writeEndElement();
        writer.writeStartElement("revision");
        writer.writeCharacters(versionSpec);
        writer.writeEndElement();
        writer.writeStartElement("transactionId");
        writer.writeCharacters("0"); // This seems to always be 0
        writer.writeEndElement();
        writer.writeStartElement("userId");
        writer.writeCharacters(userName);
        writer.writeEndElement();
        writer.writeEndElement(); // versionEntry

    }

    private void makeTopic(int moid, File resultFile, String title)
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

    private void makeParagraphs(XMLStreamWriter writer) throws XMLStreamException {
        int paraCnt = ThreadLocalRandom.current().nextInt(1, 10);
        for (int i = 0; i < paraCnt; i++) {
            writer.writeStartElement("p");
            writer.writeAttribute("class", "- topic/p ");
            writer.writeCharacters(getRandomWords(7, 30));
            writer.writeEndElement();
        }
    }

    /**
     *
     * @param min Minimum number of words to get
     * @param max Maximum number of words to get
     * @return A string of words picked at random with no fewer than min and
     * no more than max words.
     */
    public static String getRandomWords(int min, int max) {
        int wordCount = ThreadLocalRandom.current().nextInt(min, max+1);
        ArrayList<String> words = new ArrayList<String>();
        for (int i = 0; i < wordCount; i++) {
            int wordNdx = ThreadLocalRandom.current().nextInt(0, wordList.size());
            words.add(wordList.get(wordNdx));
        }
        return StringUtils.join(words, " ");
    }

    private int nextMoId() {
        return moid++;
    }

    private File getRandomDir(int min, int max, File parent) {
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

}
