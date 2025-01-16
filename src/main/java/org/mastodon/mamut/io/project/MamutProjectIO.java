/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
import org.mastodon.io.IOUtils;

import ij.ImagePlus;
import mpicbg.spim.data.XmlHelpers;

public class MamutProjectIO
{

	public static final String MAMUTPROJECT_TAG = "MamutProject";

	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_NAME = "version";

	public static final String MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT = "0.3";

	public static final String SPIMDATAFILE_TAG = "SpimDataFile";

	private static final String SPACE_UNITS_TAG = "SpaceUnits";

	private static final String TIME_UNITS_TAG = "TimeUnits";

	/**
	 * Returns a new project object for a new empty project operating on the
	 * image dataset specified by the BDV file.
	 * 
	 * @param datasetXmlFile
	 *            the path to the BDV file (the XML file).
	 * @return a new {@link MamutProject}.
	 */
	public static final MamutProject fromBdvFile( final File datasetXmlFile )
	{
		return new MamutProject( null, datasetXmlFile );
	}

	/**
	 * Returns a new project object for a new empty project operating on the
	 * image data in the specified {@link ImagePlus}.
	 * 
	 * @param imp
	 *            the image.
	 * @return a new {@link MamutProject}.
	 */
	public static MamutImagePlusProject fromImagePlus( final ImagePlus imp )
	{
		return new MamutImagePlusProject( imp );
	}

	/**
	 * Saves the project description via the specified project writer.
	 * 
	 * @param project
	 *            the project to save.
	 * @param writer
	 *            the project writer.
	 * @throws IOException
	 *             if an error occurs while writing the project description.
	 */
	public static final void save( final MamutProject project, final MamutProject.ProjectWriter writer ) throws IOException
	{
		final Document doc = new Document( toXml( project ) );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		IOUtils.mkdirs( project.getProjectRoot().getParentFile().getAbsolutePath() );
		final OutputStream os = writer.getProjectXmlOutputStream();
		xout.output( doc, os );
		os.close();
	}

	/**
	 * Load a Mamut project from a <code>.mastodon</code> file.
	 * 
	 * @param projectPath
	 *            the path to the <code>.mastodon</code> file.
	 * @return a new {@link MamutProject} pointing to the <code>.mastodon</code>
	 *         file.
	 * @throws IOException
	 *             if an error occurs while reading the project file.
	 */
	public static final MamutProject load( final String projectPath ) throws IOException
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
			throw new IOException( "Problem with the " + MamutProject.PROJECT_FILE_NAME + " file:\n"
					+ e.getMessage() );
		}
		final Element root = doc.getRootElement();

		if ( !MAMUTPROJECT_TAG.equals( root.getName() ) )
			throw new IOException( "Problem with the " + MamutProject.PROJECT_FILE_NAME + " file:\n"
					+ "Expected the root element to be <" + MAMUTPROJECT_TAG + "> but got <" + root.getName() + ">. Wrong file?" );

		fromXml( project, root );

		return project;
	}

	/**
	 * Serializes the specified project to a XML element.
	 * <p>
	 * The serialized fields are:
	 * <ul>
	 * <li>the path to the dataset XML file.
	 * <li>whether the path above is relative.
	 * <li>the space physical units.
	 * <li>the time physical units.
	 * </ul>
	 * 
	 * @param project
	 *            the project to serialize.
	 * @return a new XML element.
	 */
	public static final Element toXml( final MamutProject project )
	{
		final Element root = new Element( MAMUTPROJECT_TAG );
		root.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		final File base = project.isDatasetXmlPathRelative() ? project.getProjectRoot() : null;
		root.addContent( XmlHelpers.pathElement( SPIMDATAFILE_TAG, project.getDatasetXmlFile(), base ) );
		root.addContent( XmlHelpers.textElement( SPACE_UNITS_TAG, project.getSpaceUnits() ) );
		root.addContent( XmlHelpers.textElement( TIME_UNITS_TAG, project.getTimeUnits() ) );
		return root;
	}

	/**
	 * Deserializes some fields of the Mamut project from an XML element.
	 * <p>
	 * The deserialized fields are:
	 * <ul>
	 * <li>the path to the dataset XML file.
	 * <li>whether the path above is relative.
	 * <li>the space physical units.
	 * <li>the time physical units.
	 * </ul>
	 * 
	 * @param project
	 *            the project.
	 * @param root
	 *            the XML element.
	 */
	public static final void fromXml( final MamutProject project, final Element root )
	{
		project.setDatasetXmlFile( getDatasetPathFromXml( project, root ) );
		final boolean datasetXmlPathRelative = XmlHelpers.isPathRelative( root, SPIMDATAFILE_TAG );
		project.setDatasetXmlPathRelative( datasetXmlPathRelative );
		final String spaceUnits = XmlHelpers.getText( root, SPACE_UNITS_TAG );
		final String timeUnits = XmlHelpers.getText( root, TIME_UNITS_TAG );
		project.setSpaceUnits( spaceUnits );
		project.setTimeUnits( timeUnits );
	}

	private static final File getDatasetPathFromXml( final MamutProject project, final Element root )
	{
		File datasetXml = XmlHelpers.loadPath( root, SPIMDATAFILE_TAG, project.getProjectRoot() );
		datasetXml = new File( datasetXml.getPath().replace( "\\", "/" ) );
		datasetXml = datasetXml.toPath().normalize().toFile();
		return datasetXml;
	}

	public static void main( final String[] args )
	{
		final String projectFolder = "samples/mamutproject";
		try
		{
			final MamutProject mamutProject = MamutProjectIO.load( projectFolder );
			System.out.println( mamutProject );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
