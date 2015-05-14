package net.trackmate.trackscheme;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;

// TODO: This is a duplicate of bdv.util.AbstractAction. It should move to some common parent jar.
public abstract class AbstractNamedAction extends AbstractAction
{
	public AbstractNamedAction( final String name )
	{
		super( name );
	}

	public String name()
	{
		return ( String ) getValue( NAME );
	}

	public static void put( final ActionMap map, final AbstractNamedAction a )
	{
		map.put( a.name(), a );
	}

	public static class NamedActionAdder
	{
		private final ActionMap map;

		public NamedActionAdder( final ActionMap map )
		{
			this.map = map;
		}

		public void put( final AbstractNamedAction a )
		{
			AbstractNamedAction.put( map, a );
		}
	}

	private static final long serialVersionUID = 1L;
}
