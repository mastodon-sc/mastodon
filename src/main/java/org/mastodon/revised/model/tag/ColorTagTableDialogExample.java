package org.mastodon.revised.model.tag;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ColorTagTableDialogExample
{
	static class MyElement
	{
		private String name;

		private Color color;

		public MyElement( final String name, final Color color )
		{
			this.name = name;
			this.color = color;
		}

		public String getName()
		{
			return name;
		}

		public void setName( final String name )
		{
			this.name = name;
		}

		public Color getColor()
		{
			return color;
		}

		public void setColor( final Color color )
		{
			this.color = color;
		}
	}

	static class MyElements extends ArrayList< MyElement >
	{
		private static final long serialVersionUID = 1L;

		public MyElement addElement()
		{
			final MyElement element = new MyElement( "addElement", Color.RED );
			add( element );
			return element;
		}
	}

	static class TagSetDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		public TagSetDialog( final Frame owner, final MyElements elements1, final MyElements elements2, final MyElements elements3 )
		{
			super( owner, "tag sets configuration", false );

			final JPanel tagSetPanel = new JPanel( new BorderLayout( 0, 0 ) );
			final JScrollPane scrollPaneTagSet = new JScrollPane();
			scrollPaneTagSet.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
			tagSetPanel.add( scrollPaneTagSet, BorderLayout.CENTER );
			final ColorTagTable< MyElements, MyElement > tagTable = new ColorTagTable<>(
					elements1,
					MyElements::addElement,
					MyElements::size,
					(c, e ) -> c.remove( e ),
					(c, i) -> c.get( i ),
					MyElement::setName,
					MyElement::getName,
					MyElement::setColor,
					MyElement::getColor );
			scrollPaneTagSet.setViewportView( tagTable.getTable() );

			final JPanel buttons = new JPanel();
			tagSetPanel.add( buttons, BorderLayout.SOUTH );

			final JButton b1 = new JButton( "1" );
			final JButton b2 = new JButton( "2" );
			final JButton b3 = new JButton( "3" );
			b1.addActionListener( e -> tagTable.setElements( elements1 ) );
			b2.addActionListener( e -> tagTable.setElements( elements2 ) );
			b3.addActionListener( e -> tagTable.setElements( elements3 ) );
			buttons.add( b1 );
			buttons.add( b2 );
			buttons.add( b3 );

			getContentPane().add( tagSetPanel );
			setSize( 400, 500 );
			pack();
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException
	{
		final MyElements elements = new MyElements();
		elements.add( new MyElement( "element 1", Color.BLACK ) );
		elements.add( new MyElement( "element 2", Color.ORANGE ) );
		elements.add( new MyElement( "element 3", Color.GREEN ) );

		final MyElements elements2 = new MyElements();
		elements2.add( new MyElement( "other 11", Color.GREEN ) );
		elements2.add( new MyElement( "other 12", Color.YELLOW ) );

		final MyElements elements3 = new MyElements();

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final TagSetDialog frame = new TagSetDialog( null, elements, elements2, elements3 );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLocationByPlatform( true );
		frame.setVisible( true );
	}
}
