package net.trackmate.graph.ref;

import net.trackmate.graph.Graph;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.ListenableReadOnlyGraph;
import net.trackmate.pool.MappedElement;

/**
 * TODO: javadoc
 *
 * <p>
 * <em>Important:</em> Derived classes need to define "constructor" methods
 * (preferably called {@code init(...)} and users <em>must</em> call one of
 * these immediately after creating vertices. The {@code init(...)} methods
 * <em>must</em> call {@link #initDone()} as their last step.
 * <p>
 * The reason: {@link ListenableReadOnlyGraph} should emit the
 * {@link GraphListener#vertexAdded(Vertex)} event only after some basic
 * initialization has happened on the newly created vertex. It is therefore not
 * emitted in {@link Graph#addVertex()} but instead in {@link #initDone()}.
 * <p>
 * Idiomatically, adding a vertex to a graph should look like this:<br/>
 * {@code MyVertex v = graph.addVertex().init(...);}<br/>
 * respectively:<br/>
 * {@code MyVertex v = graph.addVertex(ref).init(...);}
 * <p>
 * Like a constructor, {@code init(...)} should be called before any other
 * method, and only once. {@link AbstractListenableVertex} tries to do some
 * basic detection of violations of this rule (throwing
 * {@link IllegalStateException} if it finds anything).
 * <p>
 * TODO: It would be nice to be able to enforce this at compile time, but I
 * couldn't find a good solution to achieve that.
 *
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
		pendingInitialize = false;
	}

	NotifyPostInit< V, ? > notifyPostInit;

	/**
	 * Flag to detect missing or duplicate initialization. Is set to
	 * {@code true} in {@link #setToUninitializedState()}, is set to
	 * {@code false} in {@link #initDone()}.
	 */
	private boolean pendingInitialize;

	/**
	 * Verify that the object has been initialized.
	 *
	 * @throws IllegalStateException
	 *             if the object is in a state between beeing created (
	 *             {@link #setToUninitializedState()} and initialized (
	 *             {@link #initDone()}).
	 */
	protected void verifyInitialized() throws IllegalStateException
	{
		if ( pendingInitialize )
			throw new IllegalStateException( "TODO" );
	}

	/**
	 * This is called when a new vertex is created ({@link Graph#addVertex()},
	 * {@link Graph#addVertex(Vertex)}). We use it to set
	 * {@link #pendingInitialize} to {@code true}, which means that
	 * {@link #verifyInitialized()} will throw an exception until
	 * {@link #initDone()} clears {@link #pendingInitialize}.
	 *
	 * @throws IllegalStateException
	 *             if the object is already in a state between being created
	 *             ({@link #setToUninitializedState()}) and initialized
	 *             ({@link #initDone()}) when this method is called.
	 */
	@Override
	protected void setToUninitializedState() throws IllegalStateException
	{
		super.setToUninitializedState();
		verifyInitialized();
		pendingInitialize = true;
	}

	/**
	 * Deriving classes need to have {@code init(...)} methods, which should
	 * call this as the final step.
	 *
	 * @throws IllegalStateException
	 *             if {@link #initDone()} was already called on the object.
	 */
	@SuppressWarnings( "unchecked" )
	protected void initDone() throws IllegalStateException
	{
		if ( !pendingInitialize )
			throw new IllegalStateException( this.getClass().getSimpleName() + " already initialized! "
					+ "Please see javadoc of net.trackmate.graph.ref.AbstractListenableVertex for more information." );
		pendingInitialize = false;
		notifyPostInit.notifyVertexAdded( ( V ) this );
	}
}
