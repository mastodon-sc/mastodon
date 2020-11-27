package tpietzsch.shadergen;

import net.imglib2.RealInterval;
import org.joml.Vector3fc;

import static tpietzsch.shadergen.MinMax.MIN;

public interface Uniform3f
{
	void set( float v0, float v1, float v2 );

	/*
	 * DEFAULT METHODS
	 */

	default void set( final Vector3fc v )
	{
		set( v.x(), v.y(), v.z() );
	}

	default void set( final RealInterval interval, final MinMax minmax )
	{
		if ( interval.numDimensions() < 3 )
			throw new IllegalArgumentException(
					"Interval has " + interval.numDimensions() + " dimensions."
							+ "Expected interval of at least dimension 3." );

		if ( minmax == MIN )
			set(
					( float ) interval.realMin( 0 ),
					( float ) interval.realMin( 1 ),
					( float ) interval.realMin( 2 ) );
		else
			set(
					( float ) interval.realMax( 0 ),
					( float ) interval.realMax( 1 ),
					( float ) interval.realMax( 2 ) );
	}
}
