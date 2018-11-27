package org.mastodon.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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

	public void save( final MamutProject project, final MamutProject.ProjectWriter writer ) throws IOException
	{
		final Document doc = new Document( toXml( project ) );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		mkdirs( project.getProjectRoot().getParentFile().getAbsolutePath() );
		final OutputStream os = writer.getProjectXmlOutputStream();
		xout.output( doc, os );
		os.close();
	}

	public MamutProject load( final String projectPath ) throws IOException
	{
		final MamutProject project = new MamutProject( projectPath );

		final SAXBuilder sax = new SAXBuilder();
		Document doc;
		try ( final MamutProject.ProjectReader reader = project.openForReading() )
		{
			doc = sax.build( reader.getProjectXmlInputStream() );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
		final Element root = doc.getRootElement();

		if ( !MAMUTPROJECT_TAG.equals( root.getName() ) )
			throw new IOException( "expected <" + MAMUTPROJECT_TAG + "> root element. wrong file?" );

		fromXml( project, root );

		return project;
	}

	public Element toXml( final MamutProject project )
	{
		final Element root = new Element( MAMUTPROJECT_TAG );
		root.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), project.getProjectRoot() ) );
		root.addContent( XmlHelpers.textElement( SPACE_UNITS_TAG, project.getSpaceUnits() ) );
		root.addContent( XmlHelpers.textElement( TIME_UNITS_TAG, project.getTimeUnits() ) );
		return root;
	}

	public void fromXml( final MamutProject project, final Element root )
	{
		final File datasetXmlFile = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, project.getProjectRoot() ).toPath().normalize().toFile();
		project.setDatasetXmlFile( datasetXmlFile );
		final String spaceUnits = XmlHelpers.getText( root, SPACE_UNITS_TAG );
		final String timeUnits = XmlHelpers.getText( root, TIME_UNITS_TAG );
		project.setSpaceUnits( spaceUnits );
		project.setTimeUnits( timeUnits );
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
