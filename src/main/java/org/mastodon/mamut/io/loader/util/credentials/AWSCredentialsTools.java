/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Mastodon developers
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
package org.mastodon.mamut.io.loader.util.credentials;

import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.amazonaws.auth.BasicAWSCredentials;

public class AWSCredentialsTools
{
    public static BasicAWSCredentials getBasicAWSCredentials()
    {
        final JLabel lblUsername = new JLabel( "Username" );
        final JTextField textFieldUsername = new JTextField();
        final JLabel lblPassword = new JLabel( "Password" );
        final JPasswordField passwordField = new JPasswordField();
        final Object[] ob = { lblUsername, textFieldUsername, lblPassword, passwordField };
        final int result = JOptionPane.showConfirmDialog( null, ob, "Please input credentials", JOptionPane.OK_CANCEL_OPTION );

        if ( result == JOptionPane.OK_OPTION )
        {
            final String username = textFieldUsername.getText();
            final char[] password = passwordField.getPassword();
            try
            {
                return new BasicAWSCredentials( username, String.valueOf( password ) );
            }
            finally
            {
                Arrays.fill( password, '0' );
            }
        }
        else
        {
            return null;
        }
    }

}
