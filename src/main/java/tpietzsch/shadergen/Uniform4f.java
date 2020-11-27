package tpietzsch.shadergen;

import net.imglib2.RealInterval;
import org.joml.Vector4fc;

import static tpietzsch.shadergen.MinMax.MIN;

public interface Uniform4f
{
	void set( float v0, float v1, float v2, float v3 );

	default void set( final Vector4fc v )
	{
		set( v.x(), v.y(), v.z(), v.w() );
	}

	default void set( final RealInterval interval, final MinMax minmax )
	{
		if ( interval.numDimensions() < 4 )
			throw new IllegalArgumentException(
					"Interval has " + interval.numDimensions() + " dimensions."
							+ "Expected interval of at least dimension 4." );

		if ( minmax == MIN )
			set(
					( float ) interval.realMin( 0 ),
					( float ) interval.realMin( 1 ),
					( float ) interval.realMin( 2 ),
					( float ) interval.realMin( 3 ) );
		else
			set(
					( float ) interval.realMax( 0 ),
					( float ) interval.realMax( 1 ),
					( float ) interval.realMax( 2 ),
					( float ) interval.realMax( 3 ) );
	}
}
