package net.trackmate.revised.ui.selection;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class NavigationGroupHandlerImp implements NavigationGroupReceiver, NavigationGroupEmitter, NavigationGroupHandler
{
	private final TIntSet groups = new TIntHashSet();

	@Override
	public TIntSet getGroups()
	{
		return groups;
	}

	@Override
	public boolean isInGroup( final int group )
	{
		return groups.contains( group );
	}

	@Override
	public void setGroupActive( final int lockId, final boolean activate )
	{
		if ( activate )
			groups.add( lockId );
		else
			groups.remove( lockId );
	}
}
