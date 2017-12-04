package org.mastodon.revised.model.mamut;

import org.mastodon.revised.trackscheme.wrap.DefaultModelGraphProperties;

public class ModelGraphTrackSchemeProperties extends DefaultModelGraphProperties< Spot, Link >
{
	private final ModelGraph modelGraph;

	public ModelGraphTrackSchemeProperties( final ModelGraph modelGraph )
	{
		this.modelGraph = modelGraph;
	}

	@Override
	public Link addEdge( final Spot source, final Spot target, final Link ref )
	{
		return modelGraph.addEdge( source, target, ref );
	}

	@Override
	public Link insertEdge( final Spot source, final int sourceOutIndex, final Spot target, final int targetInIndex, final Link ref )
	{
		return modelGraph.insertEdge( source, sourceOutIndex, target, targetInIndex, ref );
	}

	@Override
	public Link initEdge( final Link link )
	{
		return link.init();
	}

	@Override
	public void removeEdge( final Link link )
	{
		modelGraph.remove( link );
	}

	@Override
	public void removeVertex( final Spot spot )
	{
		modelGraph.remove( spot );
	}

	@Override
	public void notifyGraphChanged()
	{
		modelGraph.notifyGraphChanged();
	}
}
