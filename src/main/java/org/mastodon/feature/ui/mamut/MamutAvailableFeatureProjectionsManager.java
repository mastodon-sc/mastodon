package org.mastodon.feature.ui.mamut;

import static org.mastodon.feature.ui.AvailableFeatureProjectionsImp.createAvailableFeatureProjections;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.feature.ui.AvailableFeatureProjectionsManager;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.util.Listeners;

public class MamutAvailableFeatureProjectionsManager implements AvailableFeatureProjectionsManager
{
	private final FeatureSpecsService featureSpecsService;

	private final FeatureColorModeManager featureColorModeManager;

	private MamutAppModel appModel;

	private final Listeners.List< AvailableFeatureProjectionsListener > listeners;

	public MamutAvailableFeatureProjectionsManager(
			final FeatureSpecsService featureSpecsService,
			final FeatureColorModeManager featureColorModeManager )
	{
		this.featureSpecsService = featureSpecsService;
		this.featureColorModeManager = featureColorModeManager;
		this.listeners = new Listeners.List<>();
	}

	/**
	 * Sets the current {@code MamutAppModel}. This will update the available
	 * projections and listen to the appModel's {@code FeatureModel}.
	 *
	 * @param appModel
	 *            the current {@code MamutAppModel} (or {@code null}).
	 */
	public void setAppModel( final MamutAppModel appModel )
	{
		this.appModel = appModel;

		if ( appModel != null )
			appModel.getModel().getFeatureModel().listeners().add( this::notifyAvailableFeatureProjectionsChanged );

		notifyAvailableFeatureProjectionsChanged();
	}

	/**
	 * Exposes the list of listeners that are notified when a change happens to
	 */
	@Override
	public Listeners< AvailableFeatureProjectionsListener > listeners()
	{
		return listeners;
	}

	@Override
	public AvailableFeatureProjections getAvailableFeatureProjections()
	{
		final int numSources = ( appModel != null )
				? appModel.getSharedBdvData().getSources().size()
				: 1;
		final FeatureModel featureModel = ( appModel != null )
				? appModel.getModel().getFeatureModel()
				: null;
		return createAvailableFeatureProjections(
				featureSpecsService,
				numSources,
				featureModel,
				featureColorModeManager,
				Spot.class,
				Link.class );
	}

	private void notifyAvailableFeatureProjectionsChanged()
	{
		listeners.list.forEach( AvailableFeatureProjectionsListener::availableFeatureProjectionsChanged );
	}
}
