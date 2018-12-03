package org.mastodon.grouping;

/**
 * A factory for a specific kind of {@code ForwardingModel<T>}. It can create
 * both, forwarding models (used as model with switchable backend by views) and
 * backing models (used to store shared state for a group or an individual
 * view).
 * <p>
 * {@code GroupableModelFactory} instances are also used as keys for the types
 * of models managed by a {@code GroupHandle}. See
 * {@link GroupHandle#getModel(GroupableModelFactory)},
 * {@link GroupManager#registerModel(GroupableModelFactory)}.
 * </p>
 *
 * @author Tobias Pietzsch
 * @param <T>
 *            the type of the backing model.
 */
public interface GroupableModelFactory< T >
{
	public T createBackingModel();

	public ForwardingModel< T > createForwardingModel();
}
