package org.mastodon.kdtree;

import java.util.Iterator;

import org.mastodon.RefPool;
import org.mastodon.pool.MappedElement;

import net.imglib2.RealLocalizable;

public class KDTreeValidIterator<
		O extends RealLocalizable,
		T extends MappedElement >
	implements Iterator< O >
{
	public static <
			O extends RealLocalizable,
			T extends MappedElement >
		KDTreeValidIterator< O, T > create( final KDTree< O, T > kdtree )
	{
		return new KDTreeValidIterator< O, T >( kdtree );
	}

	private final O ref;

	private final O nextref;

	private O next;

	private final Iterator< KDTreeNode< O, T > > kdtreeIter;

	private final RefPool< O > objPool;

	private boolean hasNext;

	public KDTreeValidIterator( final KDTree< O, T > tree )
	{
		this.objPool = tree.getObjectPool();
		ref = objPool.createRef();
		nextref = objPool.createRef();
		kdtreeIter = tree.iterator();
		hasNext = prepareNext();
	}

	private boolean prepareNext()
	{
		while ( kdtreeIter.hasNext() )
		{
			final KDTreeNode< O, T > n = kdtreeIter.next();
			if ( n.isValid() )
			{
				next = objPool.getObject( n.getDataIndex(), nextref );
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public O next()
	{
		if ( hasNext )
		{
			final O current = objPool.getObject( objPool.getId( next ), ref );
			hasNext = prepareNext();
			return current;
		}
		return null;
	}
}
