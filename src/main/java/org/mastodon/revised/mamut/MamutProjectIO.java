package org.mastodon.revised.mamut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import mpicbg.spim.data.XmlHelpers;

public class MamutProjectIO
{
	public static final String MAMUTPROJECT_TAG = "MamutProject";
	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_NAME = "version";
	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT = "0.2";
	public static final String BASEPATH_TAG = "BasePath";
	public static final String SPIMDATAFILE_TAG = "SpimDataFile";
	public static final String RAWMODELFILE_TAG = "RawModelFile";

	public void save( final MamutProject project ) throws IOException
	{
		final Document doc = new Document( toXml( project ) );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		mkdirs( project.getProjectFile().getAbsolutePath() );
		xout.output( doc, new FileOutputStream( project.getProjectFile() ) );
	}

	public MamutProject load( final String projectPath ) throws IOException
	{
		final File projectFile = new File( projectPath );
		final String projectXmlFilename = projectFile.isDirectory()
				? new File( projectPath, MamutProject.PROJECT_FILE_NAME ).getAbsolutePath()
				: projectPath;

		final SAXBuilder sax = new SAXBuilder();
		Document doc;
		try
		{
			doc = sax.build( projectXmlFilename );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
		final Element root = doc.getRootElement();

		if ( root.getName() != MAMUTPROJECT_TAG )
			throw new RuntimeException( "expected <" + MAMUTPROJECT_TAG + "> root element. wrong file?" );

		return fromXml( root, new File( projectXmlFilename ).getParentFile() );
	}

	public Element toXml( final MamutProject project )
	{
		final Element root = new Element( MAMUTPROJECT_TAG );
		root.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), project.getProjectFolder() ) );
		root.addContent( XmlHelpers.pathElement( RAWMODELFILE_TAG, project.getRawModelFile(), project.getProjectFolder() ) );
		return root;
	}

	public MamutProject fromXml( final Element root, final File projectFolder )
	{
		final File datasetXmlFile = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, projectFolder );
		return new MamutProject( projectFolder, datasetXmlFile );
	}

	public static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}

	public static void main( final String[] args )
	{
		final String projectFolder = "samples/mamutproject";
//		final String bdvFile = "samples/datasethdf5.xml";
//		final MamutProject project = new MamutProject( new File( projectFolder ), new File( bdvFile ) );
//		try
//		{
//			new MamutProjectIO().save( project );
//		}
//		catch ( final IOException e )
//		{
//			e.printStackTrace();

//		}

		try
		{
			final MamutProject mamutProject = new MamutProjectIO().load( projectFolder );
			System.out.println( mamutProject );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
