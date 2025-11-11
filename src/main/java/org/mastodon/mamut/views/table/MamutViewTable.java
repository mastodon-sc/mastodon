package org.mastodon.mamut.views.table;

import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.app.WindowManager;
import org.mastodon.views.table.MastodonViewTable;
import org.mastodon.views.table.TableModelGraphProperties;

/**
 * Mamut app-specific table view.
 */
public class MamutViewTable extends MastodonViewTable< Model, ModelGraph, Spot, Link >
{

	protected MamutViewTable( final Model dataModel, final WindowManager< ? > windowManager, final TableModelGraphProperties< Spot > properties )
	{
		super( dataModel, windowManager, properties, false );
	}
}
