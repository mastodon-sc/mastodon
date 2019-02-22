package org.mastodon.revised.bdv;

import java.util.List;

import org.scijava.ui.behaviour.util.Behaviours;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformEventHandler;

public interface BehaviourTransformEventHandlerMamut extends TransformEventHandler< AffineTransform3D >
{

	public void install( Behaviours behaviours );

	/**
	 * Utility that returns <code>true</code> if all the sources specified are 2D.
	 * 
	 * @param sources
	 *                          the list of sources to test.
	 * @param numTimepoints
	 *                          the number of time-points in the dataset.
	 * @return <code>true</code> if all the sources specified are 2D.
	 */
	public static boolean is2D( final List< SourceAndConverter< ? > > sources, final int numTimepoints )
	{
		for ( final SourceAndConverter< ? > sac : sources )
		{
			final Source< ? > source = sac.getSpimSource();
			for ( int t = 0; t < numTimepoints; t++ )
			{
				if ( source.isPresent( t ) )
				{
					final RandomAccessibleInterval< ? > level = source.getSource( t, 0 );
					if ( level.dimension( 2 ) > 1 )
						return false;

					break;
				}
			}
		}
		return true;
	}
}
