package net.trackmate.kdtree;

import java.util.Iterator;

import net.imglib2.RealLocalizable;
import net.trackmate.Ref;
import net.trackmate.RefPool;
import net.trackmate.pool.MappedElement;

public class KDTreeValidIterator<
		O extends Ref< O > & RealLocalizable,
		T extends MappedElement >
	implements Iterator< O >
{
	public static <
			O extends Ref< O > & RealLocalizable,
			T extends MappedElement >
		KDTreeValidIterator< O, T > create( final KDTree< O, T > kdtree, final RefPool< O > objPool )
	{
		return new KDTreeValidIterator< O, T >( kdtree, objPool );
	}

	private final O ref;

	private final O nextref;

	private final Iterator< KDTreeNode< O, T > > kdtreeIter;

	private final RefPool< O > objPool;

	private boolean hasNext;

	public KDTreeValidIterator( final KDTree< O, T > kdtree, final RefPool< O > objPool )
	{
		this.objPool = objPool;
		ref = objPool.createRef();
		nextref = objPool.createRef();
		kdtreeIter = kdtree.iterator();
		hasNext = prepareNext();
	}

	private boolean prepareNext()
	{
		while ( kdtreeIter.hasNext() )
		{
			final KDTreeNode< O, T > n = kdtreeIter.next();
			if ( n.isValid() )
			{
				objPool.getByInternalPoolIndex( n.getDataIndex(), nextref );
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
			ref.refTo( nextref );
			hasNext = prepareNext();
		}
		return ref;
	}
}
