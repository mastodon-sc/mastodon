package org.mastodon.grouping;

/**
 * Implements a model interface that forwards to a switchable
 * ({@link #linkTo(Object, boolean)}) backing model. This is used for grouping,
 * where the view-facing model is a {@code ForwardingModel} that redirects to
 * the model of the active group.
 * <p>
 * If Java would allow for it, this should be defined as
 * "{@code ForwardingModel<T> extends T}". Instead, {@link #asT()} provides a
 * {@code T}-typed reference.
 * </p>
 *
 * @author Tobias Pietzsch
 * @param <T>
 *            the type of the backing model.
 */
public interface ForwardingModel< T >
{
	/**
	 * Switch this {@code ForwardingModel} to a new backing {@code model}.
	 *
	 * @param model
	 *            the new backing model.
	 * @param copyCurrentStateToNewModel
	 *            whether the state of the current backing model should be
	 *            copied to the new backing model. (For example, when a view
	 *            moves from a shared group to its own singleton group, the
	 *            state is transferred.)
	 */
	public void linkTo( final T model, final boolean copyCurrentStateToNewModel );

	/**
	 * Get a {@code T} that forwards to whichever {@code model} was last
	 * {@link #linkTo(Object, boolean) linked to}.
	 *
	 * @return a {@code T} (usually {@code this}) that forwards linked
	 *         {@code model}
	 */
	@SuppressWarnings( "unchecked" )
	public default T asT()
	{
		return ( T ) this;
	}
}
