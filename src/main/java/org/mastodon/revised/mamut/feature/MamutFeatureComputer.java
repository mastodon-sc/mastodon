package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Model;

/**
 * A specific interface for Mastodon feature computers. They receive the image
 * data, on which they can base their feature values (<i>e.g.</i> spot mean
 * intensity).
 *
 * @author Jean-Yves Tinevez
 */
public interface MamutFeatureComputer extends FeatureComputer< Model >
{

	/**
	 * Sets the image data to be used by this computer.
	 *
	 * @param bdvData
	 *            the image data.
	 */
	public default void setSharedBigDataViewerData( final SharedBigDataViewerData bdvData )
	{
		// Ignored by default.
	}
}
