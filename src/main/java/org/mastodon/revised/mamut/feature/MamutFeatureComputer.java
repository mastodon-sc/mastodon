package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Model;

/**
 * A specific abstract class for Mastodon feature computers. They receive the image
 * data, on which they can base their feature values (<i>e.g.</i> spot mean
 * intensity).
 *
 * @author Jean-Yves Tinevez
 */
public abstract class MamutFeatureComputer implements FeatureComputer< Model >
{

	protected String spaceUnits = "";

	protected String timeUnits = "frame";

	protected SharedBigDataViewerData bdvData;

	private final String key;

	protected MamutFeatureComputer(final String key)
	{
		this.key = key;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	/**
	 * Sets the image data to be used by this computer.
	 *
	 * @param bdvData
	 *            the image data.
	 */
	public void setSharedBigDataViewerData( final SharedBigDataViewerData bdvData )
	{
		this.bdvData = bdvData;
		this.spaceUnits = bdvData.getSources().get( 0 ).getSpimSource().getVoxelDimensions().unit();
	}
}
