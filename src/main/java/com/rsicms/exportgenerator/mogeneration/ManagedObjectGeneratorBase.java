package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.ManagedObjectGenerator;
import com.rsicms.exportgenerator.api.MoType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
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

    protected int moid = 1000; // Start with MO 1000 so MO IDs look realistic.

    protected static ArrayList<String> wordList = new ArrayList<String>();

    static {
        String wordsFilePath = "/resources/data-files/words.txt";
        InputStream inStream = ManagedObjectGeneratorBase.class.getResourceAsStream(wordsFilePath);
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


    public ManagedObjectGeneratorBase(GenerationParameters generationParameters) {
        this.generationParameters = generationParameters;
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

    protected void makeVersionEntry(XMLStreamWriter writer,
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

    protected int nextMoId() {
        return moid++;
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

    protected void makeResourceFileForXmlMo(File moDir,
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
            int moid = nextMoId();
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
        String title = getRandomWords(2,5);
        makeXmlContent(moid, contentXml, title);
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
            makeXmlContent(moid, verFile, title);
        }
        makeResourceFileForXmlMo(moDir, moid, title, versionSpecs);
        this.generationParameters.addMo(moid, moType);
    }
}
