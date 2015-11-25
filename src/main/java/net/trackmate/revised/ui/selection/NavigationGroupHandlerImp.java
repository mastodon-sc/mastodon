package net.trackmate.revised.ui.selection;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashSet;

public class NavigationGroupHandlerImp implements NavigationGroupHandler
{
	private final TIntSet groups = new TIntHashSet();

	private final HashSet< NavigationGroupChangeListener > listeners = new HashSet< NavigationGroupChangeListener >();

	@Override
	public TIntSet getGroups()
	{
		return groups;
	}

	@Override
	public boolean isGroupActive( final int group )
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

	@Override
	public boolean addNavigationGroupChangeListener( final NavigationGroupChangeListener l )
	{
		return listeners.add( l );
	}

	@Override
	public boolean removeNavigationGroupChangeListener( final NavigationGroupChangeListener l )
	{
		return listeners.remove( l );
	}
}
