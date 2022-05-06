package org.mastodon.views.table;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.mastodon.RefPool;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.ui.coloring.ColorGenerator;
import org.mastodon.undo.UndoPointMarker;

public class TablePanelBuilder< O >
{

	private final RefPool< O > pool;

	private ObjTags< O > tags;

	private Function< O, String > labelGenerator;

	private BiConsumer< O, String > labelSetter;

	private UndoPointMarker undo;

	private ColorGenerator< O > coloring;

	private TablePanelBuilder( final RefPool< O > pool )
	{
		this.pool = pool;
		this.labelGenerator = o -> o.toString();
		this.labelSetter = ( o, str ) -> {};
	}

	public TablePanelBuilder< O > tags( final ObjTags< O > tags )
	{
		this.tags = tags;
		return this;
	}

	public TablePanelBuilder< O > labelGetter( final Function< O, String > labelGetter )
	{
		this.labelGenerator = labelGetter;
		return this;
	}

	public TablePanelBuilder< O > labelSetter( final BiConsumer< O, String > labelSetter )
	{
		this.labelSetter = labelSetter;
		return this;
	}

	public TablePanelBuilder< O > coloring( final ColorGenerator< O > coloring )
	{
		this.coloring = coloring;
		return this;
	}

	public TablePanelBuilder< O > undo( final UndoPointMarker undo )
	{
		this.undo = undo;
		return this;
	}

	public FeatureTagTablePanel< O > get()
	{
		if ( pool == null )
			throw new IllegalArgumentException( "The object pool cannot be null." );
		final FeatureTagTablePanel< O > tablePanel = new FeatureTagTablePanel<>(
				tags,
				pool,
				labelGenerator,
				labelSetter,
				undo,
				coloring );
		return tablePanel;
	}

	public static < O > TablePanelBuilder< O > create( final RefPool< O > pool )
	{
		return new TablePanelBuilder<>( pool );
	}
}
