package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.model.mamut.Spot;

/**
 * Marker class for MaMuT feature computers to sort between different
 * targets.
 *
 * @author Jean-Yves Tinevez
 */
public abstract class SpotFeatureComputer extends MamutFeatureComputer
{
	public SpotFeatureComputer( final String key )
	{
		super( key );
	}

	@Override
	public final Class< ? > getTargetClass()
	{
		return Spot.class;
	}
}
