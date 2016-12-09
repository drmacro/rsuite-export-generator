package com.rsicms.exportgenerator.mogeneration;

import com.rsicms.exportgenerator.GenerationParameters;
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

    @Test
    public void testCreateManagedObject() throws Exception {

        URL props = TestManagedObjectGeneratorBase.class.getResource("/generation.properties");
        assertNotNull("Didn't get props file", props);
        File propsFile = new File(props.getFile());
        GenerationParameters genParms = new GenerationParameters(propsFile);
        genParms.setOutputDirectory(new File((String)(genParms.get("outdir"))));
        int moid = 1;
        genParms.addMo(moid, MoType.XML);
        ManagedObject mo = genParms.getManagedObject(moid);
        assertNotNull("Didn't get an MO back", mo);
        assertEquals("MO ID doesn't match", mo.getID(), moid);
        ArrayList<ManagedObject> xmlMOs = genParms.getManagedObjectsOfType(MoType.XML);
        assertNotNull("Didn't get MO list back", xmlMOs);
        assertEquals("Expected 1 XML MO", xmlMOs.size(), 1);


    }
}
