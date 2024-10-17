/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Mastodon developers
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
/*-
 * #%L
 * Readers and writers for image data in MoBIE projects
 * %%
 * Copyright (C) 2021 - 2023 EMBL
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
package org.mastodon.mamut.io.loader.util.mobie;

import java.util.function.BiConsumer;

import org.janelia.saalfeldlab.n5.DataBlock;
import org.janelia.saalfeldlab.n5.DataType;
import org.jetbrains.annotations.NotNull;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileDoubleArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileIntArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileLongArray;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.GenericIntType;
import net.imglib2.type.numeric.integer.GenericLongType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public abstract class ArrayCreator< A, T extends NativeType< T > >
{
	protected final CellGrid cellGrid;

	protected final DataType dataType;

	protected final BiConsumer< ArrayImg< T, ? >, DataBlock< ? > > copyFromBlock;

	public ArrayCreator( final CellGrid cellGrid, final DataType dataType )
	{
		this.cellGrid = cellGrid;
		this.dataType = dataType;
		this.copyFromBlock = createCopy( dataType );
	}

	@SuppressWarnings( "unchecked" )
	@NotNull
	public A VolatileDoubleArray( final DataBlock< ? > dataBlock, final long[] cellDims, final int n )
	{
		switch ( dataType )
		{
		case UINT8:
		case INT8:
			final byte[] bytes = new byte[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.bytes( bytes, cellDims ) ), dataBlock );
			return ( A ) new VolatileByteArray( bytes, true );
		case UINT16:
		case INT16:
			final short[] shorts = new short[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.shorts( shorts, cellDims ) ), dataBlock );
			return ( A ) new VolatileShortArray( shorts, true );
		case UINT32:
		case INT32:
			final int[] ints = new int[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.ints( ints, cellDims ) ), dataBlock );
			return ( A ) new VolatileIntArray( ints, true );
		case UINT64:
		case INT64:
			final long[] longs = new long[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.longs( longs, cellDims ) ), dataBlock );
			return ( A ) new VolatileLongArray( longs, true );
		case FLOAT32:
			final float[] floats = new float[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.floats( floats, cellDims ) ), dataBlock );
			return ( A ) new VolatileFloatArray( floats, true );
		case FLOAT64:
			final double[] doubles = new double[ n ];
			copyFromBlock.accept( Cast.unchecked( ArrayImgs.doubles( doubles, cellDims ) ), dataBlock );
			return ( A ) new VolatileDoubleArray( doubles, true );
		default:
			throw new IllegalArgumentException();
		}
	}

	public A createEmptyArray( final long[] gridPosition )
	{
		final long[] cellDims = getCellDims( gridPosition );
		final int n = ( int ) ( cellDims[ 0 ] * cellDims[ 1 ] * cellDims[ 2 ] );
		switch ( dataType )
		{
		case UINT8:
		case INT8:
			return Cast.unchecked( new VolatileByteArray( new byte[ n ], true ) );
		case UINT16:
		case INT16:
			return Cast.unchecked( new VolatileShortArray( new short[ n ], true ) );
		case UINT32:
		case INT32:
			return Cast.unchecked( new VolatileIntArray( new int[ n ], true ) );
		case UINT64:
		case INT64:
			return Cast.unchecked( new VolatileLongArray( new long[ n ], true ) );
		case FLOAT32:
			return Cast.unchecked( new VolatileFloatArray( new float[ n ], true ) );
		case FLOAT64:
			return Cast.unchecked( new VolatileDoubleArray( new double[ n ], true ) );
		default:
			throw new IllegalArgumentException();
		}
	}

	public long[] getCellDims( final long[] gridPosition )
	{
		return null;
	}

	private static < T extends NativeType< T >, I extends RandomAccessibleInterval< T > & IterableInterval< T > > BiConsumer< I, DataBlock< ? > > createCopy(
			final DataType dataType )
	{
		switch ( dataType )
		{
		case INT8:
		case UINT8:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final byte[] data = ( byte[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends GenericByteType< ? > > c = ( Cursor< ? extends GenericByteType< ? > > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().setByte( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		case INT16:
		case UINT16:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final short[] data = ( short[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends GenericShortType< ? > > c = ( Cursor< ? extends GenericShortType< ? > > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().setShort( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		case INT32:
		case UINT32:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final int[] data = ( int[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends GenericIntType< ? > > c = ( Cursor< ? extends GenericIntType< ? > > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().setInt( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		case INT64:
		case UINT64:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final long[] data = ( long[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends GenericLongType< ? > > c = ( Cursor< ? extends GenericLongType< ? > > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().setLong( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		case FLOAT32:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final float[] data = ( float[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends FloatType > c = ( Cursor< ? extends FloatType > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().set( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		case FLOAT64:
			return ( a, b ) -> {
				if ( sizeEquals( a, b ) )
				{
					final double[] data = ( double[] ) b.getData();
					@SuppressWarnings( "unchecked" )
					final Cursor< ? extends DoubleType > c = ( Cursor< ? extends DoubleType > ) a.cursor();
					for ( int i = 0; i < data.length; ++i )
						c.next().set( data[ i ] );
				}
				else
					copyIntersection( a, b, dataType );
			};
		default:
			throw new IllegalArgumentException( "Type " + dataType.name() + " not supported!" );
		}
	}

	private static boolean sizeEquals( final Interval a, final DataBlock< ? > b )
	{
		final int[] dataBlockSize = b.getSize();
		for ( int d = 0; d < dataBlockSize.length; ++d )
		{
			if ( a.dimension( d ) != dataBlockSize[ d ] )
				return false;
		}
		return true;
	}

	private static < T extends NativeType< T >, I extends RandomAccessibleInterval< T > & IterableInterval< T > > void copyIntersection(
			final I a,
			final DataBlock< ? > b,
			final DataType dataType )
	{
		@SuppressWarnings( "unchecked" )
		final ArrayImg< T, ? > block = dataBlock2ArrayImg( b, dataType );
		final IntervalView< T > za = Views.zeroMin( a );
		final FinalInterval intersection = Intervals.intersect( block, za );
		final Cursor< T > c = Views.interval( za, intersection ).cursor();
		final Cursor< T > d = Views.interval( block, intersection ).cursor();
		while ( c.hasNext() )
			c.next().set( d.next() );
	}

	@SuppressWarnings( "rawtypes" )
	private static ArrayImg dataBlock2ArrayImg(
			final DataBlock< ? > dataBlock,
			final DataType dataType )
	{
		final int[] dataBlockSize = dataBlock.getSize();
		final long[] dims = new long[ dataBlockSize.length ];
		for ( int d = 0; d < dataBlockSize.length; ++d )
			dims[ d ] = dataBlockSize[ d ];
		switch ( dataType )
		{
		case INT8:
			return ArrayImgs.bytes( ( byte[] ) dataBlock.getData(), dims );
		case UINT8:
			return ArrayImgs.unsignedBytes( ( byte[] ) dataBlock.getData(), dims );
		case INT16:
			return ArrayImgs.shorts( ( short[] ) dataBlock.getData(), dims );
		case UINT16:
			return ArrayImgs.unsignedShorts( ( short[] ) dataBlock.getData(), dims );
		case INT32:
			return ArrayImgs.ints( ( int[] ) dataBlock.getData(), dims );
		case UINT32:
			return ArrayImgs.unsignedInts( ( int[] ) dataBlock.getData(), dims );
		case INT64:
			return ArrayImgs.longs( ( long[] ) dataBlock.getData(), dims );
		case UINT64:
			return ArrayImgs.unsignedLongs( ( long[] ) dataBlock.getData(), dims );
		case FLOAT32:
			return ArrayImgs.floats( ( float[] ) dataBlock.getData(), dims );
		case FLOAT64:
			return ArrayImgs.doubles( ( double[] ) dataBlock.getData(), dims );
		default:
			return null;
		}
	}
}
