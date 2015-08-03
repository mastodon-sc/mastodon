package net.trackmate.bdv.wrapper;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.Vertex;

/**
 * Interface for classes that can localize a {@link Vertex} in space.
 *
 * @param <V>
 *            the type of the vertex to localize.
 */
public interface VertexLocalizer< V >
{
	public void localize( V v, final float[] position );

	public void localize( V v, final double[] position );

	public float getFloatPosition( V v, final int d );

	public double getDoublePosition( V v, final int d );

	public int numDimensions( V v );
	
	public static class DefaultVertexLocalizer< V extends RealLocalizable > implements VertexLocalizer< V >
	{

		@Override
		public void localize( final V v, final float[] position )
		{
			v.localize( position );
		}

		@Override
		public void localize( final V v, final double[] position )
		{
			v.localize( position );
		}

		@Override
		public float getFloatPosition( final V v, final int d )
		{
			return v.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final V v, final int d )
		{
			return v.getDoublePosition( d );
		}

		@Override
		public int numDimensions( final V v )
		{
			return v.numDimensions();
		}

	}
}
