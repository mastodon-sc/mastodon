package org.mastodon.revised.mamut.feature;

/**
 * Marker class for MaMuT feature computers to sort between different targets.
 *
 * @author Jean-Yves Tinevez
 */
public abstract class LinkFeatureComputer extends MamutFeatureComputer
{

	protected LinkFeatureComputer( final String key )
	{
		super( key );
	}
}
