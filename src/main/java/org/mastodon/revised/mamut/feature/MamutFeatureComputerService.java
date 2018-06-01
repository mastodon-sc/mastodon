package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.mamut.Model;

public interface MamutFeatureComputerService extends FeatureComputerService< Model, MamutFeatureComputer >
{

	/**
	 * Sets the image data to be used by the feature computers.
	 *
	 * @param bdvData
	 *            the image data.
	 */
	public void setSharedBdvData( SharedBigDataViewerData sharedBdvData );
}