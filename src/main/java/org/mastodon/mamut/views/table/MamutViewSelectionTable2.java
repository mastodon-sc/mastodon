package org.mastodon.mamut.views.table;

import org.mastodon.app.UIModel;
import org.mastodon.app.views.table.MastodonViewTable2;
import org.mastodon.app.views.table.TableModelGraphProperties;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Mamut app-specific selection table view.
 */
public class MamutViewSelectionTable2 extends MastodonViewTable2< Model, ModelGraph, Spot, Link >
{

	protected MamutViewSelectionTable2( final Model dataModel, final UIModel< ? > uiModel, final TableModelGraphProperties< Spot > properties )
	{
		super( dataModel, uiModel, properties, true );
	}
}
