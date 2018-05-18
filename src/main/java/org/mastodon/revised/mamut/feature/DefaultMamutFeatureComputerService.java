package org.mastodon.revised.mamut.feature;

import java.util.Collection;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.AbstractFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.plugin.Plugin;

@Plugin(type = MamutFeatureComputerService.class)
public class DefaultMamutFeatureComputerService extends AbstractFeatureComputerService< Model, MamutFeatureComputer > implements MamutFeatureComputerService
{
	private SharedBigDataViewerData sharedBdvData;

	@Override
	public void initialize()
	{
		initializeFeatureComputers( SpotFeatureComputer.class );
		initializeFeatureComputers( LinkFeatureComputer.class );
	}

	@Override
	public void setSharedBdvData( final SharedBigDataViewerData sharedBdvData )
	{
		this.sharedBdvData = sharedBdvData;
	}

	@Override
	public Collection< MamutFeatureComputer > getFeatureComputers()
	{
		final Collection< MamutFeatureComputer > fcs = super.getFeatureComputers();
		// Pass image data before exposing them.
		for ( final MamutFeatureComputer fc : fcs )
			fc.setSharedBigDataViewerData( sharedBdvData );
		return fcs;
	}
}
