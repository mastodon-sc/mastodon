package org.mastodon.revised.mamut.feature;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.AbstractFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutFeatureComputerService.class )
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
	protected void prepareComputer( final MamutFeatureComputer computer )
	{
		computer.setSharedBigDataViewerData( sharedBdvData );
	}
}
