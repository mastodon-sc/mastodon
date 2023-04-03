package org.mastodon.app.ui;

import org.mastodon.mamut.model.Model;
import org.mastodon.model.TimepointModel;

import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *  A label that displays (and updates according to the timepoint model) the current timepoint and the number of spots in the
 *  current timepoint.
 *  
 * @author Stefan Hahmann 
 */
public class TimepointAndNumberOfSpotsLabel extends JLabel
{
	@Nonnull
	private final TimepointModel timepointModel;

	@Nonnull
	private final Model model;

	/**
	 * Creates a new label that displays the current timepoint and the number of spots in the current timepoint.
	 * Registers itself as a listener to the timepoint model.
	 * 
	 * @param timepointModel
	 *            the timepoint model to listen to
	 * @param model
	 *            the model to get the number of spots from
	 */
	public TimepointAndNumberOfSpotsLabel( @Nonnull final TimepointModel timepointModel, @Nonnull final Model model )
	{
		this.timepointModel = timepointModel;
		this.model = model;
		this.timepointModel.listeners().add( this::updateTimepointAndNumberOfSpotsLabel );
		updateTimepointAndNumberOfSpotsLabel();
	}

	private void updateTimepointAndNumberOfSpotsLabel()
	{
		SwingUtilities
				.invokeLater( () -> setText( "timepoint: " + timepointModel.getTimepoint() + "   spots: "
						+ model.getSpatioTemporalIndex().getSpatialIndex( timepointModel.getTimepoint() ).size() ) );
	}
}
