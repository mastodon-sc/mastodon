package tpietzsch.shadergen;

import net.imglib2.RealInterval;
import org.joml.Vector2fc;

import static tpietzsch.shadergen.MinMax.MIN;

public interface Uniform2f
{
	void set( float v0, float v1 );

	default void set( final Vector2fc v )
	{
		set( v.x(), v.y() );
	}

	default void set( final RealInterval interval, final MinMax minmax )
	{
		if ( interval.numDimensions() < 2 )
			throw new IllegalArgumentException(
					"Interval has " + interval.numDimensions() + " dimensions."
							+ "Expected interval of at least dimension 2." );

		if ( minmax == MIN )
			set(
					( float ) interval.realMin( 0 ),
					( float ) interval.realMin( 1 ) );
		else
			set(
					( float ) interval.realMax( 0 ),
					( float ) interval.realMax( 1 ) );
	}
}
