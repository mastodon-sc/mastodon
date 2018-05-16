package org.mastodon.revised.mamut.feature;

import java.util.Collection;
import java.util.Set;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.feature.AbstractFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.ProgressListener;
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
	public boolean compute( final Model model, final FeatureModel featureModel, final Set< MamutFeatureComputer > computers, final ProgressListener progressListener )
	{
		// Pass image data before calling compute.
		final Collection< MamutFeatureComputer > fcs = getFeatureComputers();
		for ( final MamutFeatureComputer computer : fcs )
			computer.setSharedBigDataViewerData( sharedBdvData );
		// Compute.
		return super.compute( model, featureModel, computers, progressListener );
	}
}
