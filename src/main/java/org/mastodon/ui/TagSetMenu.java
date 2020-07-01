package org.mastodon.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.util.ColorIcon;
import org.mastodon.util.MnemonicsAssigner;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class TagSetMenu< V extends Vertex< E >, E extends Edge< V > > implements TagSetModel.TagSetModelListener
{

	private final JMenu menu;

	private final TagSetModel< V, E > tagSetModel;

	private final SelectionModel< V, E > selectionModel;

	private final UndoPointMarker undo;

	private final ReentrantReadWriteLock lock;

	public TagSetMenu( final JMenu menu, final TagSetModel< V, E > tagSetModel, final SelectionModel< V, E > selectionModel, final ReentrantReadWriteLock lock, final UndoPointMarker undo )
	{
		this.menu = menu;
		this.tagSetModel = tagSetModel;
		this.selectionModel = selectionModel;
		this.lock = lock;
		this.undo = undo;
		rebuild();
	}

	public void rebuild()
	{
		menu.removeAll();
		final MnemonicsAssigner menuMnemo = new MnemonicsAssigner();

		final TagSetStructure tagSetStructure = tagSetModel.getTagSetStructure();
		final List< TagSetStructure.TagSet > tagSets = tagSetStructure.getTagSets();
		for ( final TagSetStructure.TagSet ts : tagSets )
		{
			final JMenu tsMenu = new JMenu( ts.getName() );
			menuMnemo.add( tsMenu );

			final MnemonicsAssigner subMenuMnemo = new MnemonicsAssigner();
			for ( final Tag tag : ts.getTags() )
			{
				final JMenuItem menuItem = new JMenuItem( new SetTagAction<>( tagSetModel, ts, tag, selectionModel, lock, undo ) );
				subMenuMnemo.add( menuItem );
				tsMenu.add( menuItem );
			}

			tsMenu.add( new JSeparator() );

			final JMenuItem clearTagMenuItem = new JMenuItem( new ClearTagAction<>( tagSetModel, ts, selectionModel, lock, undo ) );
			subMenuMnemo.add( clearTagMenuItem );
			tsMenu.add( clearTagMenuItem );
			menu.add( tsMenu );
			subMenuMnemo.assignMnemonics();
		}
		menuMnemo.assignMnemonics();
	}

	@Override
	public void tagSetStructureChanged()
	{
		rebuild();
	}

	private static class SetTagAction< V extends Vertex< E >, E extends Edge< V > > extends AbstractAction
	{

		private static final long serialVersionUID = 1L;

		private final Tag tag;

		private final TagSet tagSet;

		private final SelectionModel< V, E > selectionModel;

		private final UndoPointMarker undo;

		private final TagSetModel< V, E > tagSetModel;

		private final ReentrantReadWriteLock lock;

		public SetTagAction( final TagSetModel< V, E > tagSetModel, final TagSet tagSet, final Tag tag, final SelectionModel< V, E > selectionModel, final ReentrantReadWriteLock lock, final UndoPointMarker undo )
		{
			super( tag.label(), new ColorIcon( new Color( tag.color(), true ) ) );
			this.tagSetModel = tagSetModel;
			this.tagSet = tagSet;
			this.tag = tag;
			this.selectionModel = selectionModel;
			this.lock = lock;
			this.undo = undo;
		}

		@Override
		public void actionPerformed( final ActionEvent evtt )
		{
			lock.readLock().lock();
			try
			{
				final ObjTagMap< V, Tag > vertexTags = tagSetModel.getVertexTags().tags( tagSet );
				final ObjTagMap< E, Tag > edgeTags = tagSetModel.getEdgeTags().tags( tagSet );
				selectionModel.getSelectedVertices().forEach( v -> vertexTags.set( v, tag ) );
				selectionModel.getSelectedEdges().forEach( e -> edgeTags.set( e, tag ) );
			}
			finally
			{
				lock.readLock().unlock();
			}
			undo.setUndoPoint();
		}
	}

	private static class ClearTagAction< V extends Vertex< E >, E extends Edge< V > > extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private final TagSet tagSet;

		private final SelectionModel< V, E > selectionModel;

		private final UndoPointMarker undo;

		private final TagSetModel< V, E > tagSetModel;

		private final ReentrantReadWriteLock lock;

		public ClearTagAction( final TagSetModel< V, E > tagSetModel, final TagSet tagSet, final SelectionModel< V, E > selectionModel, final ReentrantReadWriteLock lock, final UndoPointMarker undo )
		{
			super( "Clear tags for " + tagSet.getName() );
			this.tagSetModel = tagSetModel;
			this.tagSet = tagSet;
			this.selectionModel = selectionModel;
			this.lock = lock;
			this.undo = undo;
		}

		@Override
		public void actionPerformed( final ActionEvent evtt )
		{
			lock.readLock().lock();
			try
			{
				final ObjTagMap< V, Tag > vertexTags = tagSetModel.getVertexTags().tags( tagSet );
				final ObjTagMap< E, Tag > edgeTags = tagSetModel.getEdgeTags().tags( tagSet );
				selectionModel.getSelectedVertices().forEach( v -> vertexTags.set( v, null ) );
				selectionModel.getSelectedEdges().forEach( e -> edgeTags.set( e, null ) );
			}
			finally
			{
				lock.readLock().unlock();
			}
			undo.setUndoPoint();
		}
	}
}
