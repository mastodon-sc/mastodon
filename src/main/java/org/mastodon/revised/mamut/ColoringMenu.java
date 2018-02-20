package org.mastodon.revised.mamut;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.util.HasSelectedState;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ColoringMenu implements TagSetModel.TagSetModelListener
{
	private final JMenu menu;

	private final ColoringModel coloringModel;

	private final ArrayList< Runnable > cleanup;

	public ColoringMenu(
			final JMenu menu,
			final ColoringModel coloringModel )
	{
		this.menu = menu;
		this.coloringModel = coloringModel;
		cleanup = new ArrayList<>();
		rebuild();
	}

	public void rebuild()
	{
		menu.removeAll();
		cleanup.forEach( r -> r.run() );
		cleanup.clear();

		final TagSetStructure tss = coloringModel.getTagSetStructure();
		final List< TagSetStructure.TagSet > tagSets = tss.getTagSets();
		for ( final TagSetStructure.TagSet ts : tagSets )
			addColorAction( new ColorAction(
					ts.getName(),
					() -> coloringModel.getTagSet() == ts,
					() -> coloringModel.colorByTagSet( ts ) ) );

		if ( !tagSets.isEmpty() )
			menu.add( new JSeparator() );

		addColorAction( new ColorAction(
				"None",
				() -> coloringModel.noColoring(),
				() -> coloringModel.colorByNone() ) );

	}

	private void addColorAction( final ColorAction action )
	{
		coloringModel.listeners().add( action );
		cleanup.add( () -> coloringModel.listeners().remove( action ) );
		final JMenuItem item = new JCheckBoxMenuItem( action );
		item.setText( action.name() );
		item.setSelected( action.isSelected() );
		action.selectListeners().add( item::setSelected );
		menu.add( item );
	}

	@Override
	public void tagSetStructureChanged()
	{
		rebuild();
	}

	public static class ColorAction extends AbstractNamedAction implements HasSelectedState, ColoringModel.ColoringChangedListener
	{
		private final Listeners.List< Listener > selectListeners;

		private final BooleanSupplier isSelected;

		private final Runnable onSelect;

		public ColorAction(
				final String name,
				final BooleanSupplier isSelected,
				final Runnable onSelect )
		{
			super( name );
			this.isSelected = isSelected;
			this.onSelect = onSelect;
			selectListeners = new Listeners.SynchronizedList<>();
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			onSelect.run();
		}

		@Override
		public boolean isSelected()
		{
			return isSelected.getAsBoolean();
		}

		@Override
		public Listeners< Listener > selectListeners()
		{
			return selectListeners;
		}

		@Override
		public void coloringChanged()
		{
			selectListeners.list.forEach( l -> l.setSelected( isSelected() ) );
		}
	}
}
