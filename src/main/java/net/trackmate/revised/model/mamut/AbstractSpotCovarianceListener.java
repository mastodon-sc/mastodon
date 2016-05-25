package net.trackmate.revised.model.mamut;

import net.trackmate.revised.model.AbstractSpotListener;

public interface AbstractSpotCovarianceListener extends AbstractSpotListener< Spot >
{
	public void beforeCovarianceChange( Spot vertex );
}
