package org.mastodon.revised.trackscheme.display.ui.dummygraph;

import java.util.concurrent.atomic.AtomicInteger;

import org.mastodon.graph.object.AbstractObjectEdge;

public class DummyEdge extends AbstractObjectEdge< DummyEdge, DummyVertex >
{

	private static final AtomicInteger idGenerator = new AtomicInteger();

	private final int id;

	protected DummyEdge( final DummyVertex source, final DummyVertex target )
	{
		super( source, target );
		id = idGenerator.getAndIncrement();
	}

	public int getId()
	{
		return id;
	}

}
