package org.mastodon.ui.coloring;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.util.HasSelectedState;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ColoringMenu implements TagSetModel.TagSetModelListener, FeatureModelListener, FeatureColorModeManager.FeatureColorModesListener
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
					() -> true,
					() -> coloringModel.colorByTagSet( ts ) ) );

		if ( !tagSets.isEmpty() )
			menu.add( new JSeparator() );

		final FeatureColorModeManager featureColorModeManager = coloringModel.getFeatureColorModeManager();
		final List< FeatureColorMode > l1 = featureColorModeManager.getBuiltinStyles();
		final List< FeatureColorMode > l2 = featureColorModeManager.getUserStyles();
		Stream.concat( l1.stream(), l2.stream() ).forEach( mode ->
				addColorAction( new ColorAction(
						mode.getName(),
						() -> coloringModel.getFeatureColorMode() == mode,
						() -> coloringModel.isValid( mode ),
						() -> coloringModel.colorByFeature( mode ) ) ) );

		if ( !( l1.isEmpty() && l2.isEmpty() ) )
			menu.add( new JSeparator() );

		addColorAction( new ColorAction(
				"None",
				() -> coloringModel.noColoring(),
				() -> true,
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

	@Override
	public void featureModelChanged()
	{
		rebuild();
	}

	@Override
	public void featureColorModesChanged()
	{
		rebuild();
	}

	public static class ColorAction extends AbstractNamedAction implements HasSelectedState, ColoringModel.ColoringChangedListener
	{

		private static final long serialVersionUID = 1L;

		private final Listeners.List< Listener > selectListeners;

		private final BooleanSupplier isSelected;

		private final Runnable onSelect;

		private final BooleanSupplier isEnabled;

		public ColorAction(
				final String name,
				final BooleanSupplier isSelected,
				final BooleanSupplier isEnabled,
				final Runnable onSelect )
		{
			super( name );
			this.isSelected = isSelected;
			this.isEnabled = isEnabled;
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
		public boolean isEnabled()
		{
			return isEnabled.getAsBoolean();
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
