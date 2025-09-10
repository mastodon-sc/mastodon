package org.mastodon.mamut.views.table;

import org.mastodon.app.ui.UIModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Mamut app-specific table view.
 */
public class MamutViewTable2 extends MastodonViewTable2< Model, ModelGraph, Spot, Link >
{

	protected MamutViewTable2( final Model dataModel, final UIModel< ? > uiModel, final TableModelGraphProperties< Spot > properties )
	{
		super( dataModel, uiModel, properties, false );
	}
}
