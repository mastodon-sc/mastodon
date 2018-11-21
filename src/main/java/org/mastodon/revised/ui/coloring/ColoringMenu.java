package org.mastodon.revised.ui.coloring;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.UpdateListener;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.Projections;
import org.mastodon.revised.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.mastodon.revised.util.HasSelectedState;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ColoringMenu implements TagSetModel.TagSetModelListener, UpdateListener, FeatureModelListener
{
	private final JMenu menu;

	private final ColoringModel coloringModel;

	private final ArrayList< Runnable > cleanup;

	private final Projections projections;

	public ColoringMenu(
			final JMenu menu,
			final ColoringModel coloringModel,
			final FeatureModel featureModel )
	{
		this.menu = menu;
		this.coloringModel = coloringModel;
		this.projections = new ProjectionsFromFeatureModel( featureModel );
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
		final ArrayList< FeatureColorMode > modes = new ArrayList<>( l1.size() + l2.size() );
		modes.addAll( l1 );
		modes.addAll( l2 );
		for ( final FeatureColorMode mode : modes )
			addColorAction( new ColorAction(
					mode.getName(),
					() -> coloringModel.getFeatureColorMode() == mode,
					() -> isValid( mode ),
					() -> coloringModel.colorByFeature( mode ) ) );

		if ( !modes.isEmpty() )
			menu.add( new JSeparator() );

		addColorAction( new ColorAction(
				"None",
				() -> coloringModel.noColoring(),
				() -> true,
				() -> coloringModel.colorByNone() ) );

	}

	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	private boolean isValid( final FeatureColorMode mode )
	{
		if ( mode.getVertexColorMode() != FeatureColorMode.VertexColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getVertexFeatureProjection() ) )
			return false;

		if ( mode.getEdgeColorMode() != FeatureColorMode.EdgeColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getEdgeFeatureProjection() ) )
			return false;

		return true;
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
	public void featureColorModeChanged()
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


