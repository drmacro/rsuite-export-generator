package com.rsicms.exportgenerator.generation;

import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.BrowseTreeGenerator;
import com.rsicms.exportgenerator.api.MoType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a random browse tree.
 */
public class DefaultBrowseTreeGenerator implements BrowseTreeGenerator {

    private final int browseWidth;
    private final int browseDepth;
    private final int maxChildren;

    private final GenerationParameters generationParameters;

    public DefaultBrowseTreeGenerator(GenerationParameters generationParameters) {
        this.generationParameters = generationParameters;
        browseWidth = generationParameters.getBrowseWidth();
        browseDepth = generationParameters.getBrowseDepth();
        maxChildren = generationParameters.getMaxContainerChildren();
    }

    public void generateBrowseTree() throws Exception {

        System.out.print("\nBrowse tree: ");
        int depth = 0;
        File outDir = new File(generationParameters.getOutputDirectory(), "rsuite.content");
        ManagedObject root = new ManagedObject(4, MoType.CA, "/");
        makeContainers(root, depth, outDir);

    }


    private void makeContainers(ManagedObject parent, int depth, File outDir) throws Exception {
        if (depth > browseDepth) return;

        int numContainers = ThreadLocalRandom.current().nextInt(0, browseWidth+1);
        ArrayList<ManagedObject> containers = new ArrayList<ManagedObject>();
        for (int i = 0; i < numContainers; i++) {
            containers.add(makeContainer(depth + 1, outDir));
        }
        // Now write the rsuite.node file for the root container.

        makeRSuiteNodeFile(outDir, parent, containers);
    }

    private void makeRSuiteNodeFile(File outDir, ManagedObject container, ArrayList<ManagedObject> children)
            throws Exception
    {
        String userName = "fakeexportuser";

        File resultFile = new File(outDir, "rsuite.node");
        FileOutputStream fos = new FileOutputStream(resultFile);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new BufferedOutputStream(
                        fos), "UTF-8");
        try {
            writer.writeStartDocument();
            writer.writeStartElement("contentResource");
            writer.writeStartElement("nestedIds");
            for (ManagedObject child : children) {
                writer.writeStartElement("id");
                writer.writeCharacters(child.getDisplayName());
                writer.writeEndElement(); // id
            }
            writer.writeEndElement(); //
            writer.writeStartElement("systemMetadata");
            writer.writeStartElement("createdate");
            writer.writeCharacters("2016-09-27T16:30:04.490Z");
            writer.writeEndElement(); // createdate
            writer.writeStartElement("id");
            writer.writeCharacters("" + container.getID());
            writer.writeEndElement(); // id
            writer.writeStartElement("username");
            writer.writeCharacters("system");
            writer.writeEndElement(); // "username"
            writer.writeEndElement(); //
            writer.writeStartElement("versions");
            GenerationHelper.makeVersionEntry(writer,
                    container.getDisplayName(),
                    "1.0",
                    "rs_ca",
                    userName
                    );
            writer.writeEndElement(); // "versions"
            writer.writeEndElement(); // "contentResource"


        } catch (Exception e) {
            throw e;
        } finally {
            writer.flush();
            writer.close();
            fos.close();
        }

    }

    private ManagedObject makeContainer(int depth, File outDir) throws Exception {

        String containerName = GenerationHelper.getRandomWords(1, 4);
        ManagedObject container =
                new ManagedObject(generationParameters.getNextMoId(),
                                  MoType.CA,
                                  containerName);

        File containerDir = new File(outDir, containerName);
        if (!containerDir.mkdirs()) {
            throw new RuntimeException("Failed to create output directory " +
                    "\"" + containerDir.getAbsolutePath() + "\"" );
        }

        int numChildren = ThreadLocalRandom.current().nextInt(0, maxChildren+1);
        ArrayList<ManagedObject> children = new ArrayList<ManagedObject>();
        ArrayList<ManagedObject> xmlMos = generationParameters.getManagedObjectsOfType(MoType.XML);

        for (int i = 0; i < numChildren; i++) {
            int p = ThreadLocalRandom.current().nextInt(0, xmlMos.size());
            children.add(xmlMos.get(p));
        }

        int numChildContainers = ThreadLocalRandom.current().nextInt(0, browseWidth+1);
        for (int i = 0; i < numChildContainers; i++) {
            children.add(makeContainer(depth + 1, containerDir));
        }

        makeContainerDoc(container, children, containerDir);
        makeRSuiteNodeFile(containerDir, container, children);

        return container;

    }

    /**
     * Make the CA XML file for the container.
     * @param container The container to make the XML file for
     * @param children The container and MO children of the container
     * @param outDir The directory to contain the container's data.
     */
    private void makeContainerDoc(ManagedObject container,
                                  ArrayList<ManagedObject> children,
                                  File outDir)
                                                    throws Exception
    {
        File resultFile = new File(outDir, container.getID() + ".resource");
        FileOutputStream fos = new FileOutputStream(resultFile);
        XMLStreamWriter writer = XMLOutputFactory.newInstance().
                createXMLStreamWriter(new BufferedOutputStream(
                        fos), "UTF-8");
        try {
            writer.writeStartDocument();
            writer.writeStartElement("rs_ca_map");
            writer.writeAttribute("xmlns:ditaarch", "http://dita.oasis-open.org/architecture/2005/");
            writer.writeAttribute("class", "- map/map rs_ca_map/rs_ca_map ");
            writer.writeAttribute("domains", "(map rs_ca_map) (map rs_ca-d) (props rs_ca-d-att)");
            writer.writeAttribute("ditaarch:DITAArchVersion", "1.2");
            writer.writeStartElement("rs_ca");
            writer.writeAttribute("xmlns:r", "http://www.rsuitecms.com/rsuite/ns/metadata");
            writer.writeAttribute("type", "ca");
            writer.writeAttribute("class", "+ map/topicref rs_ca-d/rs_ca ");
            writer.writeAttribute("r:rsuiteId", "" + container.getID());

            for (ManagedObject child : children) {
                ManagedObject moref = new ManagedObject(generationParameters.getNextMoId(), MoType.MOREF, "");
                writer.writeEmptyElement("moref");
                writer.writeAttribute("r:rsuiteId", "" + moref.getID());
                writer.writeAttribute("class", "+ map/topicref rs_ca-d/rs_moref ");
                writer.writeAttribute("href", "" + child.getID());
                // Now write a resource file for the MOREF

                ArrayList<String> versionSpecs = new ArrayList<String>();
                versionSpecs.add("1.0");
                GenerationHelper.makeResourceFileForMo(outDir, moref, versionSpecs);
            }

            writer.writeEndElement(); // rs_ca
            writer.writeEndElement(); // rs_ca_map
            writer.writeEndDocument();
        } catch (Exception e) {

        }

    }
}
