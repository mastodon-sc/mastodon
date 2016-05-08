package net.trackmate.revised.model.mamut;

import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.revised.model.undo.old.AbstractRemoveEdgeUndoableEdit;
import net.trackmate.revised.undo.UndoIdBimap;

public class RemoveLinkUndoableEdit
	extends AbstractRemoveEdgeUndoableEdit< RemoveLinkUndoableEdit, Spot, Link, ByteMappedElement >
{
	RemoveLinkUndoableEdit(
			final UndoableEditPool< RemoveLinkUndoableEdit > pool,
			final ModelGraph modelGraph,
			final UndoIdBimap< Spot > vertexUndoIdBimap,
			final UndoIdBimap< Link > edgeUndoIdBimap )
	{
		super( pool, modelGraph, vertexUndoIdBimap, edgeUndoIdBimap );
	}

	@Override
	protected void setEdgeFields( final Link edge )
	{}

	public static class RemoveLinkUndoableEditPool extends UndoableEditPool< RemoveLinkUndoableEdit >
	{
		public RemoveLinkUndoableEditPool(
				final int initialCapacity,
				final ModelGraph modelGraph,
				final UndoIdBimap< Spot > vertexUndoIdBimap,
				final UndoIdBimap< Link > edgeUndoIdBimap )
		{
			this( initialCapacity, new Factory( modelGraph, vertexUndoIdBimap, edgeUndoIdBimap ) );
		}

		private RemoveLinkUndoableEditPool( final int initialCapacity, final Factory f )
		{
			super( initialCapacity, f );
			f.pool = this;
		}

		@Override
		public RemoveLinkUndoableEdit create( final RemoveLinkUndoableEdit vertex )
		{
			return super.create( vertex );
		}

		private static class Factory implements PoolObject.Factory< RemoveLinkUndoableEdit, ByteMappedElement >
		{
			private RemoveLinkUndoableEditPool pool;

			private final ModelGraph modelGraph;

			private final UndoIdBimap< Spot > vertexUndoIdBimap;

			private final UndoIdBimap< Link > edgeUndoIdBimap;

			public Factory(
					final ModelGraph modelGraph,
					final UndoIdBimap< Spot > vertexUndoIdBimap,
					final UndoIdBimap< Link > edgeUndoIdBimap )
			{
				this.modelGraph = modelGraph;
				this.vertexUndoIdBimap = vertexUndoIdBimap;
				this.edgeUndoIdBimap = edgeUndoIdBimap;
			}

			@Override
			public int getSizeInBytes()
			{
				return RemoveLinkUndoableEdit.SIZE_IN_BYTES;
			}

			@Override
			public RemoveLinkUndoableEdit createEmptyRef()
			{
				return new RemoveLinkUndoableEdit( pool, modelGraph, vertexUndoIdBimap, edgeUndoIdBimap );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}
}
