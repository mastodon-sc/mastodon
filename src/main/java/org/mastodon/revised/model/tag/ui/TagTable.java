package org.mastodon.revised.model.tag.ui;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class TagTable< C, T > extends AbstractTagTable< C, T, AbstractTagTable< C, T, ? >.Element >
{
	public TagTable(
			final C elements,
			final Function< C, T > addElement,
			final ToIntFunction< C > size,
			final BiConsumer< C, T > remove,
			final BiFunction< C, Integer, T > get,
			final BiConsumer< T, String > setName,
			final Function< T, String > getName )
	{
		super( elements, addElement, size, remove, get, setName, getName, 0 );
	}

	@Override
	protected Elements wrap( final C wrapped )
	{
		return new Elements( wrapped ) {
			@Override
			protected Element wrap( final T wrapped )
			{
				return new Element( wrapped );
			}
		};
	}
}
