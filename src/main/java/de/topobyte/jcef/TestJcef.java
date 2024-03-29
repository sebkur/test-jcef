//Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
//reserved. Use of this source code is governed by a BSD-style license that
//can be found in the LICENSE file.

package de.topobyte.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;

import de.topobyte.shared.preferences.SharedPreferences;
import de.topobyte.swing.util.SwingUtils;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;

/**
 * This is a simple example application using JCEF. It displays a JFrame with a
 * JTextField at its top and a CefBrowser in its center. The JTextField is used
 * to enter and assign an URL to the browser UI. No additional handlers or
 * callbacks are used in this example.
 * <p>
 * The number of used JCEF classes is reduced (nearly) to its minimum and should
 * assist you to get familiar with JCEF.
 * <p>
 * For a more feature complete example have also a look onto the example code
 * within the package "tests.detailed".
 */
public class TestJcef
{

	private JTextField address;
	private JButton button;
	private CefApp cefApp;
	private CefClient client;
	private CefBrowser browser;

	private Component browerUI;
	private boolean browserFocus = true;

	/**
	 * To display a simple browser window, it suffices completely to create an
	 * instance of the class CefBrowser and to assign its UI component to your
	 * application (e.g. to your content pane). But to be more verbose, this
	 * CTOR keeps an instance of each object on the way to the browser UI.
	 */
	private TestJcef(String startURL, boolean useOSR, boolean isTransparent)
			throws UnsupportedPlatformException, CefInitializationException,
			IOException, InterruptedException
	{
		double factor = 1;
		if (SharedPreferences.isUIScalePresent()) {
			SwingUtils.setUiScale(SharedPreferences.getUIScale());
			factor = SharedPreferences.getUIScale();
		}

		// Create a new CefAppBuilder instance
		CefAppBuilder builder = new CefAppBuilder();

		// Configure the builder instance
		builder.setInstallDir(new File("jcef-bundle")); // Default
		builder.setProgressHandler(new ConsoleProgressHandler()); // Default
		builder.getCefSettings().windowless_rendering_enabled = false;
		// Default - select OSR mode

		// Set an app handler. Do not use CefApp.addAppHandler(...), it will
		// break your code on MacOSX!
		builder.setAppHandler(new MavenCefAppHandlerAdapter() {
			// CefApp is responsible for the global CEF context. It loads all
			// required native libraries, initializes CEF accordingly, starts a
			// background task to handle CEF's message loop and takes care of
			// shutting down CEF after disposing it.
			@Override
			public void stateHasChanged(org.cef.CefApp.CefAppState state)
			{
				System.out.println(state);
				// Shutdown the app if the native CEF part is terminated
				if (state == CefAppState.TERMINATED) {
					System.exit(0);
				}
			}
		});

		// Build a CefApp instance using the configuration above
		cefApp = builder.build();

		// (2) JCEF can handle one to many browser instances simultaneous. These
		// browser instances are logically grouped together by an instance of
		// the class CefClient. In your application you can create one to many
		// instances of CefClient with one to many CefBrowser instances per
		// client. To get an instance of CefClient you have to use the method
		// "createClient()" of your CefApp instance. Calling an CTOR of
		// CefClient is not supported.
		//
		// CefClient is a connector to all possible events which come from the
		// CefBrowser instances. Those events could be simple things like the
		// change of the browser title or more complex ones like context menu
		// events. By assigning handlers to CefClient you can control the
		// behavior of the browser. See tests.detailed.MainFrame for an example
		// of how to use these handlers.
		client = cefApp.createClient();

		CefMessageRouter msgRouter = CefMessageRouter.create();
		client.addMessageRouter(msgRouter);

		// (3) One CefBrowser instance is responsible to control what you'll see
		// on the UI component of the instance. It can be displayed off-screen
		// rendered or windowed rendered. To get an instance of CefBrowser you
		// have to call the method "createBrowser()" of your CefClient
		// instances.
		//
		// CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
		// and many more which are used to control the behavior of the displayed
		// content. The UI is held within a UI-Compontent which can be accessed
		// by calling the method "getUIComponent()" on the instance of
		// CefBrowser. The UI component is inherited from a java.awt.Component
		// and therefore it can be embedded into any AWT UI.
		browser = client.createBrowser(startURL, useOSR, isTransparent);
		browerUI = browser.getUIComponent();

		// (4) For this minimal browser, we need only a text field to enter an
		// URL we want to navigate to and a CefBrowser window to display the
		// content of the URL. To respond to the input of the user, we're
		// registering an anonymous ActionListener. This listener is performed
		// each time the user presses the "ENTER" key within the address field.
		// If this happens, the entered value is passed to the CefBrowser
		// instance to be loaded as URL.
		address = new JTextField(startURL, 100);
		address.addActionListener(e -> {
			browser.loadURL(address.getText());
		});
		button = new JButton("go");
		button.addActionListener(e -> {
			browser.loadURL(address.getText());
		});

		// Update the address field when the browser URL changes.
		client.addDisplayHandler(new CefDisplayHandlerAdapter() {
			@Override
			public void onAddressChange(CefBrowser browser, CefFrame frame,
					String url)
			{
				address.setText(url);
			}
		});

		// Clear focus from the browser when the address field gains focus.
		address.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e)
			{
				if (!browserFocus) {
					return;
				}
				browserFocus = false;
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.clearGlobalFocusOwner();
				address.requestFocus();
			}
		});

		// Clear focus from the address field when the browser gains focus.
		client.addFocusHandler(new CefFocusHandlerAdapter() {
			@Override
			public void onGotFocus(CefBrowser browser)
			{
				if (browserFocus) {
					return;
				}
				browserFocus = true;
				KeyboardFocusManager.getCurrentKeyboardFocusManager()
						.clearGlobalFocusOwner();
				browser.setFocus(true);
			}

			@Override
			public void onTakeFocus(CefBrowser browser, boolean next)
			{
				browserFocus = false;
			}
		});

		// Set up an adress bar
		JPanel bar = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		bar.add(address, c);
		c.weightx = 0.0;
		bar.add(button, c);

		JFrame frame = new JFrame("Test JCEF");
		// (5) All UI components are assigned to the default content pane of
		// this JFrame and afterwards the frame is made visible to the user.
		frame.getContentPane().add(bar, BorderLayout.NORTH);
		frame.getContentPane().add(browerUI, BorderLayout.CENTER);
		frame.pack();
		frame.setMinimumSize(
				new Dimension((int) (800 * factor), (int) (600 * factor)));
		frame.setVisible(true);

		// (6) To take care of shutting down CEF accordingly, it's important to
		// call the method "dispose()" of the CefApp instance if the Java
		// application will be closed. Otherwise you'll get asserts from CEF.
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				cefApp.dispose();
				frame.dispose();
			}
		});
	}

	public static void main(String[] args) throws UnsupportedPlatformException,
			CefInitializationException, IOException, InterruptedException
	{
		// Perform startup initialization on platforms that require it.

		// The simple example application is created as anonymous class and
		// points to Google as the very first loaded page. Windowed rendering
		// mode is used by default. If you want to test OSR mode set |useOsr| to
		// true and recompile.
		boolean useOsr = false;
		new TestJcef("http://www.google.com", useOsr, false);
	}
}
