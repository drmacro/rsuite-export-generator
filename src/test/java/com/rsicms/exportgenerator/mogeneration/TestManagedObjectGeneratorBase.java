package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.GenerationParameters;
import com.rsicms.exportgenerator.api.ManagedObjectGenerator;
import com.rsicms.exportgenerator.api.MoType;
import com.sun.xml.internal.ws.developer.UsesJAXBContext;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the general managed object generator API
 */
public class TestManagedObjectGeneratorBase {

    private GenerationParameters getGenerationParameters() throws Exception {
        URL props = TestManagedObjectGeneratorBase.class.getResource("/generation.properties");
        assertNotNull("Didn't get props file", props);
        File propsFile = new File(props.getFile());
        GenerationParameters genParms = new GenerationParameters(propsFile);
        File tempFile = File.createTempFile("rsi-", ".xml");
        File outDir = tempFile.getParentFile();
        genParms.setOutputDirectory(outDir);
        return genParms;
    }

    @Test
    public void testGenerationParametersMoHandling() throws Exception {

        GenerationParameters genParms = getGenerationParameters();
        int moid = 1;
        genParms.addMo(moid, MoType.XML);
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
        int moid = moGenerator.nextMoId();
        moGenerator.makeManagedObject(genParms.getOutputDirectory(), moid, MoType.XML);
        ManagedObject mo = genParms.getManagedObject(moid);
        assertNotNull(mo);
        genParms.getOutputDirectory().delete();
    }
}
