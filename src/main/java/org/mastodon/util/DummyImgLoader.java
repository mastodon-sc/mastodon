/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bdv.ViewerImgLoader;
import bdv.ViewerSetupImgLoader;
import bdv.cache.CacheControl;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileRealType;
import net.imglib2.util.ConstantUtils;

/**
 * A dummy image loader that actually does not load any real image data, but a
 * pure black background. It can handle a list of {@link Dimensions} so that
 * different view setups can be used.
 *
 * @author Stefan Hahmann
 * @author Tobias Pietzsch
 * @author Vladimir Ulman
 */
public class DummyImgLoader implements ViewerImgLoader
{
	private final List< ViewerSetupImgLoader< ?, ? > > setupImgLoaders;

	public DummyImgLoader( Dimensions dimensions )
	{
		this( new UnsignedShortType(), Collections.singletonList( dimensions ) );
	}

	/**
	 * Creates a new DummyImgLoader.
	 *
	 * @param type
	 * 		the data type of the image
	 * @param dimensionsList
	 * 		a {@link List} of {@link Dimensions}. Each entry will result in a view
	 * 		setup.
	 * @param <T>
	 * 		the data type of the image
	 */
	public < T extends RealType< T > > DummyImgLoader( final T type, final List< Dimensions > dimensionsList )
	{
		setupImgLoaders = new ArrayList<>();
		for ( Dimensions dimensions : dimensionsList )
		{
			assert ( dimensions.numDimensions() == 3 );
			setupImgLoaders.add( new DummyViewerSetupImgLoader<>( type, new FinalInterval( dimensions ) ) );
		}
	}

	@Override
	public ViewerSetupImgLoader< ?, ? > getSetupImgLoader( final int setupId )
	{
		return setupImgLoaders.get( setupId );
	}

	@Override
	public CacheControl getCacheControl()
	{
		return new CacheControl.Dummy();
	}

	private static class DummyViewerSetupImgLoader< T extends RealType< T > >
			implements ViewerSetupImgLoader< T, Volatile< T > >
	{
		private final T type;

		private final VolatileRealType< T > volatileType;

		private final RandomAccessibleInterval< T > img;

		private final RandomAccessibleInterval< Volatile< T > > volatileImg;

		private final double[][] resolution = { { 1, 1, 1 } };

		private final AffineTransform3D[] transform = { new AffineTransform3D() };

		public DummyViewerSetupImgLoader( T type, Interval interval )
		{
			this.type = type;
			this.volatileType = new VolatileRealType<>( type );
			this.img = ConstantUtils.constantRandomAccessibleInterval( type, interval );
			this.volatileImg = ConstantUtils.constantRandomAccessibleInterval( volatileType, interval );
		}

		@Override
		public RandomAccessibleInterval< T > getImage( int timepointId, int level, ImgLoaderHint... hints )
		{
			return img;
		}

		@Override
		public double[][] getMipmapResolutions()
		{
			return resolution;
		}

		@Override
		public AffineTransform3D[] getMipmapTransforms()
		{
			return transform;
		}

		@Override
		public int numMipmapLevels()
		{
			return 1;
		}

		@Override
		public RandomAccessibleInterval< Volatile< T > > getVolatileImage( int timepointId, int level,
				ImgLoaderHint... hints )
		{
			return volatileImg;
		}

		@Override
		public Volatile< T > getVolatileImageType()
		{
			return volatileType;
		}

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
	}

}
