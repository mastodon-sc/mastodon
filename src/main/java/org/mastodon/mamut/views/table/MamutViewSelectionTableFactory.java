package org.mastodon.mamut.views.table;

import org.mastodon.mamut.MamutViewTable;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class )
public class MamutViewSelectionTableFactory extends MamutViewTableFactory
{

	@Override
	public MamutViewTable create( final ProjectModel projectModel )
	{
		return new MamutViewTable( projectModel, true );
	}
}
