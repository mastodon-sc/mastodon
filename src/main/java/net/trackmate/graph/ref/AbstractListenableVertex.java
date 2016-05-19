package net.trackmate.graph.ref;

import net.trackmate.pool.MappedElement;

/**
 * TODO: javadoc
 *
 * @param <V>
 * @param <E>
 * @param <T>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AbstractListenableVertex< V extends AbstractListenableVertex< V, E, T >, E extends AbstractEdge< E, ?, ? >, T extends MappedElement >
		extends AbstractVertexWithFeatures< V, E, T >
{
	protected AbstractListenableVertex( final AbstractVertexPool< V, ?, T > pool )
	{
		super( pool );
	}

	NotifyPostInit< V, ? > notifyPostInit;

	/**
	 * Deriving classes need to have {@code init(...)} methods, which should
	 * call this as the final step.
	 */
	@SuppressWarnings( "unchecked" )
	protected void initDone()
	{
		notifyPostInit.notifyVertexAdded( ( V ) this );
	}
}
