package com.rsicms.exportgenerator.generation;

import com.rsicms.exportgenerator.ExportGenerator;
import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.MoType;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the general managed object generator API
 */
public class TestManagedObjectGeneratorBase {

    private GenerationParameters getGenerationParameters() throws Exception {
        File propsFile = getPropsFile();
        GenerationParameters genParms = new GenerationParameters(propsFile);
        File tempFile = File.createTempFile("rsi-", ".xml");
        File outDir = tempFile.getParentFile();
        genParms.setOutputDirectory(outDir);
        return genParms;
    }

    private File getPropsFile() {
        URL props = TestManagedObjectGeneratorBase.class.getResource("/tiny-generation.properties");
        assertNotNull("Didn't get props file", props);
        return new File(props.getFile());
    }

    @Test
    public void testGenerationParametersMoHandling() throws Exception {

        GenerationParameters genParms = getGenerationParameters();
        int moid = 1;
        genParms.addMo(moid, MoType.XML, "Thing 1");
        ManagedObject mo = genParms.getManagedObject(moid);
        assertNotNull("Didn't get an MO back", mo);
        assertEquals("MO ID doesn't match", mo.getID(), moid);
        ArrayList<ManagedObject> xmlMOs = genParms.getManagedObjectsOfType(MoType.XML);
        assertNotNull("Didn't get MO list back", xmlMOs);
        assertEquals("Expected 1 XML MO", xmlMOs.size(), 1);
        genParms.getOutputDirectory().delete();

    }

    @Test
    public void testCreateManagedObject() throws Exception {
        GenerationParameters genParms = getGenerationParameters();
        DitaManagedObjectGenerator moGenerator = new DitaManagedObjectGenerator(genParms);
        int moid = genParms.getNextMoId();
        moGenerator.makeManagedObject(genParms.getOutputDirectory(), moid, MoType.XML);
        ManagedObject mo = genParms.getManagedObject(moid);
        assertNotNull(mo);
        genParms.getOutputDirectory().delete();
    }

    @Test
    public void testExportGenerator() throws Exception {

        ExportGenerator exporter = new ExportGenerator(getPropsFile());
        GenerationParameters genParms = exporter.getGenerationParameters();
        File outputDir = genParms.getOutputDirectory();
        if (outputDir.exists()) {
            FileUtils.deleteDirectory(outputDir);
        }
        exporter.generateExport();

    }
}
