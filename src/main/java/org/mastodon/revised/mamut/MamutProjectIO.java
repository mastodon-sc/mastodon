package org.mastodon.revised.mamut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT = "0.3";
	public static final String SPIMDATAFILE_TAG = "SpimDataFile";
	private static final String SPACE_UNITS_TAG = "SpaceUnits";
	private static final String TIME_UNITS_TAG = "TimeUnits";

	public void save( final MamutProject project ) throws IOException
	{
		final Document doc = new Document( toXml( project ) );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		mkdirs( project.getProjectFolder().getAbsolutePath() );
		xout.output( doc, new FileOutputStream( project.getProjectFile() ) );
	}

	public MamutProject load( final String projectPath ) throws IOException
	{
		final File projectFile = new File( projectPath );
		if ( !projectFile.isDirectory() )
			throw new IOException( "expected project folder, got \"" + projectPath + "\"" );

		final String projectXmlFilename = new File( projectPath, MamutProject.PROJECT_FILE_NAME ).getAbsolutePath();
		if ( !projectFile.isDirectory() )
			throw new IOException( MamutProject.PROJECT_FILE_NAME + " not found" );

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

		if ( !MAMUTPROJECT_TAG.equals( root.getName() ) )
			throw new IOException( "expected <" + MAMUTPROJECT_TAG + "> root element. wrong file?" );

		return fromXml( root, new File( projectXmlFilename ).getParentFile() );
	}

	public Element toXml( final MamutProject project )
	{
		final Element root = new Element( MAMUTPROJECT_TAG );
		root.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), project.getProjectFolder() ) );
		return root;
	}

	public MamutProject fromXml( final Element root, final File projectFolder )
	{
		final File datasetXmlFile = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, projectFolder );
		return new MamutProject( projectFolder, datasetXmlFile );
	}

	public static final String readSpaceUnits( final MamutProject project )
	{
		final String projectXmlFilename = project.getProjectFile().getAbsolutePath();
		final SAXBuilder sax = new SAXBuilder();
		try
		{
			final Document doc = sax.build( projectXmlFilename );
			final Element root = doc.getRootElement();
			return XmlHelpers.getText( root, SPACE_UNITS_TAG );
		}
		catch ( final JDOMException | IOException e )
		{
			return "pixel";
		}
	}

	public static final String readTimeUnits( final MamutProject project )
	{
		final String projectXmlFilename = project.getProjectFile().getAbsolutePath();
		final SAXBuilder sax = new SAXBuilder();
		try
		{
			final Document doc = sax.build( projectXmlFilename );
			final Element root = doc.getRootElement();
			return XmlHelpers.getText( root, TIME_UNITS_TAG );
		}
		catch ( final JDOMException | IOException e )
		{
			return "frame";
		}
	}

	public static final void writeUnits( final MamutProject project, final String spaceUnits, final String timeUnits )
	{
		final String projectXmlFilename = project.getProjectFile().getAbsolutePath();
		final SAXBuilder sax = new SAXBuilder();
		try
		{
			// Load and edit the project file.
			final Document doc = sax.build( projectXmlFilename );
			final Element root = doc.getRootElement();
			root.addContent( XmlHelpers.textElement( SPACE_UNITS_TAG, spaceUnits ) );
			root.addContent( XmlHelpers.textElement( TIME_UNITS_TAG, timeUnits ) );

			// Resave it.
			final XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat( Format.getPrettyFormat() );
			xmlOutput.output( doc, new FileWriter( project.getProjectFile() ) );
		}
		catch ( final JDOMException | IOException e )
		{}
	}

	public static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName );
		return dir == null ? false : dir.mkdirs();
	}

	public static void main( final String[] args )
	{
		final String projectFolder = "samples/mamutproject";
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
