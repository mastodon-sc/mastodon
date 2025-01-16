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
package org.mastodon.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Cast;

/**
 * Create dummy {@link SpimDataMinimal} with a {@code BasicImgLoader} that
 * always return empty images. The image size and number of timepoints is
 * encoded in the "filename". E.g.,
 * "{@code x=1000 y=1000 z=100 sx=1 sy=1 sz=10 t=400.dummy}" means
 * {@code 1000x1000x100} images for 400 timepoints with calibration
 * {@code 1x1x10}.
 *
 * @author Tobias Pietzsch
 */
public class DummySpimData
{
	static public final String DUMMY = ".dummy";

	public static boolean isDummyString( final String name )
	{
		return name.endsWith( DUMMY );
	}

	/**
	 * Create a dummy {@link SpimDataMinimal} with a {@code BasicImgLoader} that
	 * always return empty images. The image size and number of timepoints is
	 * encoded in the "filename". E.g.,
	 * "{@code x=1000 y=1000 z=100 sx=1 sy=1 sz=10 t=400.dummy}" means
	 * {@code 1000x1000x100} images for 400 timepoints with calibration
	 * {@code 1x1x10}.
	 *
	 * @param name
	 *            the filename
	 *
	 * @return a dummy {@link SpimDataMinimal} if the name matches the pattern,
	 *         otherwise {@code null}.
	 */
	public static SpimDataMinimal tryCreate( final String name )
	{
		if ( !isDummyString( name ) )
			throw new IllegalArgumentException( "Couldn't parse dummy dataset description: '" + name + "'" );

		try
		{
			final String baseName = FilenameUtils.getBaseName( name );
			final String[] parts = baseName.split( "\\s+" );
			final int x = ( int ) get( parts, "x", 1 );
			final int y = ( int ) get( parts, "y", 1 );
			final int z = ( int ) get( parts, "z", 1 );
			final double sx = get( parts, "sx", 1 );
			final double sy = get( parts, "sy", 1 );
			final double sz = get( parts, "sz", 1 );
			final int t = ( int ) get( parts, "t", 1 );
			return tryCreate( x, y, z, sx, sy, sz, t );
		}
		catch ( final NumberFormatException e )
		{
			throw new IllegalArgumentException( "Couldn't parse dummy dataset description: '" + name + "'" );
		}
	}

	public static SpimDataMinimal tryCreate( final int x, final int y, final int z, final double sx, final double sy,
			final double sz, final int t )
	{
		final Dimensions imageSize = new FinalDimensions( x, y, z );
		final AffineTransform3D calib = new AffineTransform3D();
		calib.set( sx, 0, 0 );
		calib.set( sy, 1, 1 );
		calib.set( sz, 2, 2 );

		final File basePath = new File( "." );
		final TimePoints timepoints = new TimePoints(
				IntStream.range( 0, t ).mapToObj( TimePoint::new ).collect( Collectors.toList() ) );
		final Map< Integer, BasicViewSetup > setups = new HashMap<>();
		setups.put( 0, new BasicViewSetup( 0, "dummy", imageSize, null ) );
		final BasicImgLoader imgLoader = new DummyImgLoader( imageSize );
		final SequenceDescriptionMinimal sequenceDescription =
				new SequenceDescriptionMinimal( timepoints, setups, imgLoader, null );
		final ViewRegistrations viewRegistrations = new ViewRegistrations(
				IntStream.range( 0, t ).mapToObj( tp -> new ViewRegistration( tp, 0, calib ) )
						.collect( Collectors.toList() ) );
		return new SpimDataMinimal( basePath, sequenceDescription, viewRegistrations );
	}

	private static double get( final String[] parts, final String key, final double defaultValue )
	{
		final String prefix = key + "=";
		for ( final String part : parts )
		{
			if ( part.startsWith( prefix ) )
			{
				final String value = part.substring( prefix.length() );
				return Double.parseDouble( value );
			}
		}
		return defaultValue;
	}

	/**
	 * Returns a new {@link AbstractSpimData} object.Pixel sizes, image sizes,
	 * and image transformations are read from the given BigDataViewer XML. The
	 * actual image data is not loaded, all pixels are black.
	 * {@link DummyImgLoader} is used to provide the dummy image data.
	 * 
	 * @param spimDataXmlFilename
	 *            the spim data file name.
	 * @return a {@link AbstractSpimData} object.
	 * @throws SpimDataException
	 *             if something wrong happens when building the spim data
	 *             object.
	 */
	public static AbstractSpimData< ? > fromSpimDataXml( final String spimDataXmlFilename )
			throws SpimDataException
	{
		final File modifiedXml = getBdvXmlWithoutImageLoader( new File( spimDataXmlFilename ) );
		final AbstractSpimData< ? > spimData = new XmlIoSpimDataMinimal().load( modifiedXml.getAbsolutePath() );
		setDummyImageLoader( spimData );
		return spimData;
	}

	private static File getBdvXmlWithoutImageLoader( final File xmlFile )
	{
		final Document document = readXml( xmlFile );
		document.getRootElement()
				.getChild( "SequenceDescription" )
				.removeChildren( "ImageLoader" );
		return writeXmlToTmpFile( document );
	}

	private static void setDummyImageLoader( final AbstractSpimData< ? > spimData )
	{
		final AbstractSequenceDescription< ?, ?, BasicImgLoader > seq =
				Cast.unchecked( spimData.getSequenceDescription() );
		final List< Dimensions > dimensionsList = new ArrayList<>();
		for ( final BasicViewSetup basicViewSetup : seq.getViewSetupsOrdered() )
			dimensionsList.add( basicViewSetup.getSize() );
		seq.setImgLoader( new DummyImgLoader( new UnsignedShortType(), dimensionsList ) );
	}

	private static Document readXml( final File xmlFile )
	{
		try
		{
			return new SAXBuilder().build( xmlFile );
		}
		catch ( JDOMException | IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static File writeXmlToTmpFile( final Document document )
	{
		try
		{
			final File file = File.createTempFile( "dataset-dummy-img-loader", ".xml" );
			file.deleteOnExit();
			try (OutputStream outputStream = new FileOutputStream( file ))
			{
				new XMLOutputter( Format.getPrettyFormat() ).output( document, outputStream );
			}
			return file;
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}
}
