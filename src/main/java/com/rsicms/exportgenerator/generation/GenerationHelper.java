package com.rsicms.exportgenerator.generation;

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
 * Utilities for output generation.
 */
public class GenerationHelper {

    private static Log log = LogFactory.getLog(ManagedObjectGeneratorBase.class);

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
                GenerationHelper.wordList.add(line.trim());
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected " + e.getClass().getSimpleName() +
                    " reading words file: " + e.getMessage(), e);
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

    protected static void makeVersionEntry(XMLStreamWriter writer,
                                    String title,
                                    String versionSpec,
                                    String moTagName,
                                    String userName)
            throws XMLStreamException, FileNotFoundException {
        writer.writeStartElement("versionEntry");
        if (null != title) {
            writer.writeStartElement("displayName");
            writer.writeCharacters(title);
            writer.writeEndElement();
        }
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
        writer.writeCharacters(GenerationHelper.getRandomWords(1, 4));
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


    public static void makeResourceFileForXmlMo(File moDir,
                                                ManagedObject mo,
                                                ArrayList<String> versionSpecs)
            throws Exception {
    }

    public static void makeResourceFileForMo(File moDir,
                                             ManagedObject mo,
                                            ArrayList<String> versionSpecs)
            throws Exception {
        String userName = "fakeexportuser";

        File resourceFile = new File(moDir, mo.getID() + ".resource");
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
            writer.writeCharacters("" + mo.getID());
            writer.writeEndElement();// id
            writer.writeStartElement("username");
            writer.writeCharacters(userName);
            writer.writeEndElement();// username
            writer.writeEndElement(); // systemMetadata

            writer.writeStartElement("versions");
            // FIXME: This needs to come from the creator mo object.
            String moTagName = "topic";
            for (String versionSpec : versionSpecs) {
                GenerationHelper.makeVersionEntry(writer, mo.getDisplayName(), versionSpec, moTagName, userName);
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

}
