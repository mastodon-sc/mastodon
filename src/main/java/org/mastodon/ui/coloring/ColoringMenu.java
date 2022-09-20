/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
