package org.mastodon.views.trackscheme.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;

public class DefaultModelGraphProperties< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > >
		implements ModelGraphProperties< V, E >
{
	@Override
	public int getTimepoint( final V v )
	{
		return v.getTimepoint();
	}

	@Override
	public String getLabel( final V v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final V v, final String label )
	{
		v.setLabel( label );
	}

	@Override
	public E addEdge( final V source, final V target, final E ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E insertEdge( final V source, final int sourceOutIndex, final V target, final int targetInIndex, final E ref )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E initEdge( final E e )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEdge( final E e )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVertex( final V v )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void notifyGraphChanged()
	{
		throw new UnsupportedOperationException();
	}
}
