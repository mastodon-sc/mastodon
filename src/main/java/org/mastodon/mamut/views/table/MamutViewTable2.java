package org.mastodon.mamut.views.table;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.app.WindowManager;
import org.mastodon.views.table.MastodonViewTable2;
import org.mastodon.views.table.TableModelGraphProperties;

/**
 * Mamut app-specific table view.
 */
public class MamutViewTable2 extends MastodonViewTable2< Model, ModelGraph, Spot, Link >
{

	protected MamutViewTable2( final Model dataModel, final WindowManager< ? > windowManager, final TableModelGraphProperties< Spot > properties )
	{
		super( dataModel, windowManager, properties, false );
	}
}
