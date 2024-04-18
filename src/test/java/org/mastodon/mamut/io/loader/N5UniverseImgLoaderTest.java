package org.mastodon.mamut.io.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import uk.org.webcompere.systemstubs.rules.EnvironmentVariablesRule;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.scijava.Context;

import com.amazonaws.SdkClientException;

import mpicbg.spim.data.SpimDataException;

public class N5UniverseImgLoaderTest
{

    @Rule
    public EnvironmentVariablesRule environmentVariablesRuleId =
            new EnvironmentVariablesRule( "AWS_ACCESS_KEY_ID", "admin" );

    @Rule
    public EnvironmentVariablesRule environmentVariablesRuleSecret =
            new EnvironmentVariablesRule( "AWS_SECRET_ACCESS_KEY", "password" );

    @Test
    public void testN5HDF5Reader() throws IOException, SpimDataException
    {
        testN5UniverseImgLoader( "/org/mastodon/mamut/io/loader/mitosis_small_hdf5.xml" );
    }

    @Test
    public void testN5KeyValueReader() throws IOException, SpimDataException
    {
        testN5UniverseImgLoader( "/org/mastodon/mamut/io/loader/mitosis_small_n5.xml" );
    }

    @Test
    public void testZarrKeyValueReader() throws IOException, SpimDataException
    {
        testN5UniverseImgLoader( "/org/mastodon/mamut/io/loader/mitosis_small_omezarr.xml" );
    }

    @Test
    public void testZarrKeyValueReaderMinIOPublic() throws IOException, SpimDataException
    {
        testN5UniverseImgLoader( "/org/mastodon/mamut/io/loader/mitosis_small_omezarr_s3.xml" );
    }

    @Test
    public void variableCanBeRead()
    {
        assertEquals( System.getenv( "AWS_ACCESS_KEY_ID" ), "admin" );
        assertEquals( System.getenv( "AWS_SECRET_ACCESS_KEY" ), "password" );
    }

    @Test
    public void testZarrKeyValueReaderMinIOPrivate() throws IOException, SpimDataException
    {
        testN5UniverseImgLoader( "/org/mastodon/mamut/io/loader/mitosis_small_omezarr_s3_private.xml" );
    }

    private void testN5UniverseImgLoader( final String resource ) throws IOException, SpimDataException
    {
        final String filepath = N5UniverseImgLoaderTest.class.getResource( resource ).getPath();
        final File file = new File( filepath );
        final MamutProject project = MamutProjectIO.fromBdvFile( file );
        final ProjectModel appModel = ProjectLoader.open( project, new Context() );
        assertTrue( appModel.getProject().getDatasetXmlFile().exists() );
    }

}
