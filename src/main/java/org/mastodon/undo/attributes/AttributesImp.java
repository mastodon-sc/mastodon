package org.mastodon.undo.attributes;

import java.util.ArrayList;

import org.mastodon.graph.features.Features;

import gnu.trove.map.TIntObjectArrayMap;
import gnu.trove.map.TIntObjectMap;

/**
 * Note: In contrast to {@link Features}, this is less automatic. A graph needs
 * to register as {@link Attribute} those parts of the vertex/edge data that
 * should be undo-recorded. When changing those attributes, the
 * {@link #notifyBeforeAttributeChange(Attribute, Object)} must be called by the
 * graph, in order for the changes to be undo-recorded.
 *
 * @param <O>
 *            type of object which has the attribute (for example vertex/edge).
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class AttributesImp< O > implements Attributes< O >
{
	private int idgen;

	private final TIntObjectMap< Attribute< O > > attributes;

	private final ArrayList< AttributeChangeListener< O > > attributeChangeListeners;

	private boolean emitEvents;

	public AttributesImp()
	{
		idgen = 0;
		attributes = new TIntObjectArrayMap<>();
		attributeChangeListeners = new ArrayList<>();
		emitEvents = true;
	}

	public Attribute< O > createAttribute( final AttributeUndoSerializer< O > attributeSerializer, final String name )
	{
		final Attribute< O > attribute = new Attribute< O >( idgen++, attributeSerializer, name );
		attributes.put( attribute.getAttributeId(), attribute );
		return attribute;
	}

	@Override
	public Attribute< O > getAttributeById( final int id )
	{
		return attributes.get( id );
	}

	/**
	 * Register a {@link AttributeChangeListener} that will be notified when
	 * attribute values are changed.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	@Override
	public boolean addAttributeChangeListener( final AttributeChangeListener< O > listener )
	{
		if ( ! attributeChangeListeners.contains( listener ) )
		{
			attributeChangeListeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified {@link AttributeChangeListener} from the set of
	 * listeners.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of this
	 *         model and was successfully removed.
	 */
	@Override
	public boolean removeAttributeChangeListener( final AttributeChangeListener< O > listener )
	{
		return attributeChangeListeners.remove( listener );
	}

	/**
	 * For internal use only.
	 * <p>
	 * Notify listeners that {@code attribute} of {@code object} is about to
	 * change.
	 */
	public void notifyBeforeAttributeChange( final Attribute< O > attribute, final O object )
	{
		if ( emitEvents )
			for ( final AttributeChangeListener< O > l : attributeChangeListeners )
				l.beforeAttributeChange( attribute, object );
	}

	/**
	 * For internal use only.
	 * <p>
	 * Resume sending events to {@link AttributeChangeListener}s.
	 */
	public void pauseListeners()
	{
		emitEvents = false;
	}

	/**
	 * For internal use only.
	 * <p>
	 * Resume sending events to {@link AttributeChangeListener}s.
	 */
	public void resumeListeners()
	{
		emitEvents = true;
	}
}
