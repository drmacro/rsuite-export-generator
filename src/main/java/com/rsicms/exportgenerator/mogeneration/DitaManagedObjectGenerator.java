package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.MoType;
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

    public DitaManagedObjectGenerator(GenerationParameters generationParameters) {
        super(generationParameters);
    }

    protected void makeXmlContent(int moid, File resultFile, String title) throws Exception {
        // FIXME: Provide some heuristic for choosing what type of XML file to generate,
        // map or topic.
        makeTopic(moid, resultFile, title);
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
