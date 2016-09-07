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
	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT = "0.1";
	public static final String BASEPATH_TAG = "BasePath";
	public static final String SPIMDATAFILE_TAG = "SpimDataFile";
	public static final String RAWMODELFILE_TAG = "RawModelFile";

	public void save( final MamutProject project,  final String xmlFilename ) throws IOException
	{
		final File xmlFileDirectory = new File( xmlFilename ).getParentFile();
		final Document doc = new Document( toXml( project, xmlFileDirectory ) );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		xout.output( doc, new FileOutputStream( xmlFilename ) );
	}

	public MamutProject load( final String xmlFilename ) throws IOException
	{
		final SAXBuilder sax = new SAXBuilder();
		Document doc;
		try
		{
			doc = sax.build( xmlFilename );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
		final Element root = doc.getRootElement();

		if ( root.getName() != MAMUTPROJECT_TAG )
			throw new RuntimeException( "expected <" + MAMUTPROJECT_TAG + "> root element. wrong file?" );

		return fromXml( root, new File( xmlFilename ) );
	}

	public Element toXml( final MamutProject project, final File xmlFileDirectory )
	{
		final Element root = new Element( MAMUTPROJECT_TAG );
		root.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		root.addContent( XmlHelpers.pathElement( BASEPATH_TAG, project.getBasePath(), xmlFileDirectory ) );
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), project.getBasePath() ) );
		root.addContent( XmlHelpers.pathElement( RAWMODELFILE_TAG, project.getRawModelFile(), project.getBasePath() ) );
		return root;
	}

	public MamutProject fromXml( final Element root, final File xmlFile )
	{
		final File basePath = loadBasePath( root, xmlFile );
		final File datasetXmlFile = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, basePath );
		final File rawModelFile = XmlHelpers.loadPath( root, RAWMODELFILE_TAG, basePath );
		return new MamutProject( basePath, datasetXmlFile, rawModelFile );
	}

	private File loadBasePath( final Element root, final File xmlFile )
	{
		File xmlFileParentDirectory = xmlFile.getParentFile();
		if ( xmlFileParentDirectory == null )
			xmlFileParentDirectory = new File( "." );
		return XmlHelpers.loadPath( root, BASEPATH_TAG, ".", xmlFileParentDirectory );
	}

	public static void main( final String[] args )
	{
		final String projectfn = "samples/mamutproject.xml";
		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";

		final File basePath = new File( projectfn ).getParentFile();
		final MamutProject project = new MamutProject( basePath, new File( bdvFile ), new File( modelFile ) );
		try
		{
			new MamutProjectIO().save( project, projectfn );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
