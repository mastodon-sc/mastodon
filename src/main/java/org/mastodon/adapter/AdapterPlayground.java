package org.mastodon.adapter;

import org.mastodon.model.FocusReceiver;
import org.mastodon.model.FocusState;

public class AdapterPlayground
{
	public static class FocusStateAdapter< V, W >
			implements FocusState< W >
	{
		private final FocusState< V > focusState;
		private final RefBimap< V, W > bimap;

		public FocusStateAdapter( final FocusState< V > wrappedFocusState, final RefBimap< V, W > bimap )
		{
			this.focusState = wrappedFocusState;
			this.bimap = bimap;
		}

		@Override
		public W getFocusedVertex( final W ref )
		{
			return bimap.getRight( focusState.getFocusedVertex( bimap.extractLeftRef( ref ) ), ref );
		}

	}

	public static class FocusReceiverAdapter< V, W >
			implements FocusReceiver< W >
	{
		private final FocusReceiver< V > focusReceiver;

		private final RefBimap< V, W > bimap;

		public FocusReceiverAdapter( final FocusReceiver< V > wrappedFocusReceiver, final RefBimap< V, W > bimap )
		{
			this.focusReceiver = wrappedFocusReceiver;
			this.bimap = bimap;
		}

		@Override
		public void notifyFocusVertex( final W vertex )
		{
			focusReceiver.notifyFocusVertex( bimap.getLeft( vertex /*, bimap.extractLeftRef( vertex )*/ ) );
		}
	}

//	public static class FocusStateAdapter< V extends RefAdapter< V, WV >, WV >
//			implements FocusState< V >
//	{
//		private final FocusState< WV > wrapped;
//
//		public FocusStateAdapter( final FocusState< WV > wrapped )
//		{
//			this.wrapped = wrapped;
//		}
//
//		@Override
//		public V getFocusedVertex( final V ref )
//		{
//			final WrappedRef< WV > wref = ref.wrappedRef();
//			wref.set( wrapped.getFocusedVertex( wref.getRef() ) );
//			return ref.orNull();
//		}
//	}
//
//	public interface RefAdapter< R extends RefAdapter< R, O >, O >
//	{
//		public WrappedRef< O > wrappedRef();
//
//		default R orNull()
//		{
//			return wrappedRef().get() == null ? null : ( R ) this;
//		}
//	}
//
//	public static class WrappedRef< O >
//	{
//		private final O ref;
//
//		private O wrapped;
//
//		public WrappedRef( final O ref )
//		{
//			this.ref = ref;
//		}
//
//		O getRef()
//		{
//			return ref;
//		}
//
//		public O get()
//		{
//			return wrapped;
//		}
//
//		void set( final O wrapped )
//		{
//			this.wrapped = wrapped;
//		}
//	}
}
