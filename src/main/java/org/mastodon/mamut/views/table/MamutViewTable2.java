package org.mastodon.mamut.views.table;

import org.mastodon.app.UIModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.table.MastodonViewTable2;
import org.mastodon.views.table.TableModelGraphProperties;

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
