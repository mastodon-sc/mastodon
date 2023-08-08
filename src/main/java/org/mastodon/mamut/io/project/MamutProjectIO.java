/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.io.project;

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
		try (final MamutProject.ProjectReader reader = project.openForReading())
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
		final File base = project.isDatasetXmlPathRelative() ? project.getProjectRoot() : null;
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), base ) );
		root.addContent( XmlHelpers.textElement( SPACE_UNITS_TAG, project.getSpaceUnits() ) );
		root.addContent( XmlHelpers.textElement( TIME_UNITS_TAG, project.getTimeUnits() ) );
		return root;
	}

	public void fromXml( final MamutProject project, final Element root )
	{
		project.setDatasetXmlFile( getDatasetPathFromXml( project, root ) );
		final boolean datasetXmlPathRelative = XmlHelpers.isPathRelative( root, SPIMDATAFILE_TAG );
		project.setDatasetXmlPathRelative( datasetXmlPathRelative );
		final String spaceUnits = XmlHelpers.getText( root, SPACE_UNITS_TAG );
		final String timeUnits = XmlHelpers.getText( root, TIME_UNITS_TAG );
		project.setSpaceUnits( spaceUnits );
		project.setTimeUnits( timeUnits );
	}

	private File getDatasetPathFromXml( MamutProject project, Element root )
	{
		File datasetXml = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, project.getProjectRoot() );
		datasetXml = new File( datasetXml.getPath().replace( "\\", "/" ) );
		datasetXml = datasetXml.toPath().normalize().toFile();
		return datasetXml;
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
