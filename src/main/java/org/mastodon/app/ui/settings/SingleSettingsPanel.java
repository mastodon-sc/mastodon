/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.ui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Main panel for preferences dialogs with a single page.
 * <p>
 * The {@link SettingsPage}s is shown on the top. On the bottom,
 * "Cancel", "Apply", and "OK" buttons are shown.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class SingleSettingsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final SettingsPage page;

	private final ArrayList< Runnable > runOnOk;

	private final ArrayList< Runnable > runOnCancel;

	public SingleSettingsPanel( final SettingsPage page )
	{
		this.page = page;

		final JButton cancel = new JButton("Cancel");
		final JButton apply = new JButton("Apply");
		final JButton ok = new JButton("OK");
		final JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
		buttons.add( Box.createHorizontalGlue() );
		buttons.add( cancel );
		buttons.add( apply );
		buttons.add( ok );

		final JPanel content = new JPanel( new BorderLayout() );
		content.add( page.getJPanel(), BorderLayout.CENTER );
//		content.setBorder( new EmptyBorder( 10, 0, 10, 10 ) );
		content.setBorder( new MatteBorder( 0, 0, 1, 0, Color.LIGHT_GRAY ) );

		this.setLayout( new BorderLayout() );
		this.add( content, BorderLayout.CENTER );

		buttons.setBorder( new EmptyBorder( 10, 0, 5, 10 ) );
		this.add( buttons, BorderLayout.SOUTH );

		runOnCancel = new ArrayList<>();
		runOnOk = new ArrayList<>();

		cancel.addActionListener( e -> cancel() );
		ok.addActionListener( e -> {
			page.apply();
			runOnOk.forEach( Runnable::run );
		} );

		apply.setEnabled( false );
		page.modificationListeners().add( () -> apply.setEnabled( true ) );
		apply.addActionListener( e -> {
			apply.setEnabled( false );
			page.apply();
		} );
	}

	public void cancel()
	{
		page.cancel();
		runOnCancel.forEach( Runnable::run );
	}

	public synchronized void onOk( final Runnable runnable )
	{
		runOnOk.add( runnable );
	}

	public synchronized void onCancel( final Runnable runnable )
	{
		runOnCancel.add( runnable );
	}
}
