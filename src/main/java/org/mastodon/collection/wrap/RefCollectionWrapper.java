package org.mastodon.collection.wrap;

import java.util.Collection;

public class RefCollectionWrapper< O > extends AbstractRefCollectionWrapper< O, Collection< O > >
{
	public RefCollectionWrapper( final Collection< O > collection )
	{
		super( collection );
	}
}
