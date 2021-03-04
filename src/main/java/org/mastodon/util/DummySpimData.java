/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.util.ConstantRandomAccessible;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

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
	static final String DUMMY = ".dummy";

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
		if ( !name.endsWith( DUMMY ) )
			return null;

		try
		{
			final String[] parts = name.substring( 0, name.length() - DUMMY.length() ).split( "\\s+" );
			final int x = ( int ) get( parts, "x", 1 );
			final int y = ( int ) get( parts, "y", 1 );
			final int z = ( int ) get( parts, "z", 1 );
			final double sx = get( parts, "sx", 1 );
			final double sy = get( parts, "sy", 1 );
			final double sz = get( parts, "sz", 1 );
			final int t = ( int ) get( parts, "t", 1 );
			final Dimensions imageSize = new FinalDimensions( x, y, z );
			final AffineTransform3D calib = new AffineTransform3D();
			calib.set( sx, 0,0 );
			calib.set( sy, 1,1 );
			calib.set( sz, 2,2 );

			final File basePath = new File( "." );
			final TimePoints timepoints = new TimePoints(
					IntStream.range( 0, t ).mapToObj( TimePoint::new ).collect( Collectors.toList() ) );
			final Map< Integer, BasicViewSetup > setups = new HashMap<>();
			setups.put( 0, new BasicViewSetup( 0, "dummy", null, null ) );
			final BasicImgLoader imgLoader = new DummyImgLoader( new UnsignedShortType(), imageSize );
			final SequenceDescriptionMinimal sequenceDescription = new SequenceDescriptionMinimal( timepoints, setups, imgLoader, null );
			final ViewRegistrations viewRegistrations = new ViewRegistrations(
					IntStream.range( 0, t ).mapToObj( tp -> new ViewRegistration( tp, 0, calib ) ).collect( Collectors.toList() ) );
			return new SpimDataMinimal( basePath, sequenceDescription, viewRegistrations );
		}
		catch ( final NumberFormatException e )
		{
			return null;
		}
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

	static class DummyImgLoader implements BasicImgLoader
	{
		private final BasicSetupImgLoader< ? > setupImgLoader;

		public DummyImgLoader()
		{
			this( new UnsignedShortType(), new FinalDimensions( 100, 100, 100 ) );
		}

		public < T > DummyImgLoader( final T type, final Dimensions dimensions )
		{
			assert ( dimensions.numDimensions() == 3 );

			final RandomAccessibleInterval< T > img = Views.interval( new ConstantRandomAccessible<>( type, 3 ), new FinalInterval( dimensions ) );
			setupImgLoader = new BasicSetupImgLoader< T >()
			{
				@Override
				public RandomAccessibleInterval< T > getImage( final int timepointId, final ImgLoaderHint... hints )
				{
					return img;
				}

				@Override
				public T getImageType()
				{
					return type;
				}
			};
		}

		@Override
		public BasicSetupImgLoader< ? > getSetupImgLoader( final int setupId )
		{
			return setupImgLoader;
		}
	}
}
