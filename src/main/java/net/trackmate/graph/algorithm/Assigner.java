package net.trackmate.graph.algorithm;

import net.trackmate.graph.PoolObject;

public abstract class Assigner< O >
{
	public abstract O assign( final O value, final O target );

	@SuppressWarnings( "unchecked" )
	public static < O > Assigner< O > getFor( final O obj )
	{
		if ( obj != null && obj instanceof PoolObject )
			return RefAssign.instance;
		else
			return ObjectAssign.instance;
	}

	static class RefAssign< O extends PoolObject< O, ? > > extends Assigner< O >
	{
		@Override
		public O assign( final O value, final O target )
		{
			return target.refTo( value );
		}

		@SuppressWarnings( "rawtypes" )
		static RefAssign instance = new RefAssign();
	}

	static class ObjectAssign< O > extends Assigner< O >
	{
		@Override
		public O assign( final O value, final O target )
		{
			return value;
		}

		@SuppressWarnings( "rawtypes" )
		static ObjectAssign instance = new ObjectAssign();
	}
}
