package org.mastodon.views.trackscheme.display.style.dummygraph;

import org.mastodon.graph.object.AbstractObjectIdEdge;

public class DummyEdge extends AbstractObjectIdEdge< DummyEdge, DummyVertex >
{
	DummyEdge( final DummyVertex source, final DummyVertex target )
	{
		super( source, target );
	}
}
