/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut;

import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mastodon.model.TimepointModel;
import org.mastodon.spatial.SpatioTemporalIndex;

/**
 * A component that displays the current timepoint and the number of spots in
 * the current timepoint.
 * <p>
 * It registers itself to the given {@link TimepointModel} and thus updates
 * according it.
 * <p>
 * Extends {@link javax.swing.JPanel}
 *
 * @author Stefan Hahmann
 */
public class TimepointAndNumberOfSpotsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final TimepointModel timepointModel;

	private final JLabel numberOfSpotsLabel = new JLabel();

	private final SpatioTemporalIndex< ? > index;

	private final String objName;

	/**
	 * Creates a new component that displays the current timepoint and the
	 * number of spots at the current timepoint. Registers itself as a listener
	 * to the timepoint model.
	 *
	 * @param timepointModel
	 *            the timepoint model to listen to
	 * @param model
	 *            the model to get the number of spots from
	 */
	public TimepointAndNumberOfSpotsPanel( final TimepointModel timepointModel, final SpatioTemporalIndex< ? > index )
	{
		this.index = index;
		setLayout( new BorderLayout() );
		numberOfSpotsLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
		this.timepointModel = timepointModel;
		add( numberOfSpotsLabel, BorderLayout.CENTER );
		this.timepointModel.listeners().add( this::updateTimepointAndNumberOfSpotsLabel );
		updateTimepointAndNumberOfSpotsLabel();
		this.objName = getObjectName( index );
	}

	private static String getObjectName( final SpatioTemporalIndex< ? > index )
	{
		final Iterator< ? > it = index.iterator();
		if ( !it.hasNext() )
			return "vertices";
		return it.next().getClass().getSimpleName().toLowerCase() + "s";
	}

	private void updateTimepointAndNumberOfSpotsLabel()
	{
		SwingUtilities.invokeLater( () -> numberOfSpotsLabel.setText(
				"timepoint: "
						+ timepointModel.getTimepoint()
						+ "   "
						+ objName
						+ ": "
						+ index.getSpatialIndex( timepointModel.getTimepoint() ).size() ) );
	}
}
