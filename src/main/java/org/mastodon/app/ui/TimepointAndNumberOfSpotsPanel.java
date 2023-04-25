package org.mastodon.app.ui;

import org.mastodon.mamut.model.Model;
import org.mastodon.model.TimepointModel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *  A component that displays the current timepoint and the number of spots
 *  in the current timepoint.
 *  <p>
 *  It registers itself to the given {@link TimepointModel} and thus updates according it.
 *  <p>
 *  Extends {@link javax.swing.JPanel}
 *
 * @author Stefan Hahmann
 */
public class TimepointAndNumberOfSpotsPanel extends JPanel
{
	private final TimepointModel timepointModel;

	private final Model model;

	private final JLabel numberOfSpotsLabel = new JLabel();

	/**
	 * Creates a new component that displays the current timepoint and the number of spots at the current timepoint.
	 * Registers itself as a listener to the timepoint model.
	 *
	 * @param timepointModel
	 *            the timepoint model to listen to
	 * @param model
	 *            the model to get the number of spots from
	 */
	public TimepointAndNumberOfSpotsPanel( final TimepointModel timepointModel, final Model model )
	{
		this.timepointModel = timepointModel;
		this.model = model;
		add( numberOfSpotsLabel );
		this.timepointModel.listeners().add( this::updateTimepointAndNumberOfSpotsLabel );
		updateTimepointAndNumberOfSpotsLabel();
	}

	private void updateTimepointAndNumberOfSpotsLabel()
	{
		SwingUtilities.invokeLater( () -> numberOfSpotsLabel.setText( "timepoint: " + timepointModel.getTimepoint() + "   spots: "
				+ model.getSpatioTemporalIndex().getSpatialIndex( timepointModel.getTimepoint() ).size() ) );
	}
}
