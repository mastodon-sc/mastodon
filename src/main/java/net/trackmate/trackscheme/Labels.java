package net.trackmate.trackscheme;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Store labels of {@link TrackSchemeVertex}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class Labels
{
	private final TIntObjectMap< String > labels;

	public Labels( final int initialCapacity )
	{
		labels = new TIntObjectHashMap< String >( initialCapacity );
	}

	public void putLabel( final String label, final int index )
	{
		labels.put( index, label );
	}

	public String getLabel( final int index )
	{
		return labels.get( index );
	}
}
