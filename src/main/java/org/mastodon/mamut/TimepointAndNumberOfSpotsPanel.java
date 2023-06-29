package org.mastodon.mamut;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mastodon.mamut.model.Model;
import org.mastodon.model.TimepointModel;

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
	private final TimepointModel timepointModel;

	private final Model model;

	private final JLabel numberOfSpotsLabel = new JLabel();

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
	public TimepointAndNumberOfSpotsPanel( final TimepointModel timepointModel, final Model model )
	{
		setLayout( new BorderLayout() );
		numberOfSpotsLabel.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
		this.timepointModel = timepointModel;
		this.model = model;
		add( numberOfSpotsLabel, BorderLayout.CENTER );
		this.timepointModel.listeners().add( this::updateTimepointAndNumberOfSpotsLabel );
		updateTimepointAndNumberOfSpotsLabel();
	}

	private void updateTimepointAndNumberOfSpotsLabel()
	{
		SwingUtilities.invokeLater( () -> numberOfSpotsLabel.setText(
				"t: "
						+ timepointModel.getTimepoint()
						+ "   spots: "
						+ model.getSpatioTemporalIndex().getSpatialIndex( timepointModel.getTimepoint() ).size() ) );
	}
}
