//Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
//reserved. Use of this source code is governed by a BSD-style license that
//can be found in the LICENSE file.

package de.topobyte.jcef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.cef.CefApp;
import org.cef.CefApp.CefAppState;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefAppHandlerAdapter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;

/**
 * This is a simple example application using JCEF. It displays a JFrame with a
 * JTextField at its top and a CefBrowser in its center. The JTextField is used
 * to enter and assign an URL to the browser UI. No additional handlers or
 * callbacks are used in this example.
 *
 * The number of used JCEF classes is reduced (nearly) to its minimum and should
 * assist you to get familiar with JCEF.
 *
 * For a more feature complete example have also a look onto the example code
 * within the package "tests.detailed".
 */
public class TestJcef extends JFrame
{
	private static final long serialVersionUID = -5570653778104813836L;
	private final JTextField address;
	private final CefApp cefApp;
	private final CefClient client;
	private final CefBrowser browser;
	private final Component browerUI;
	private boolean browserFocus = true;

	/**
	 * To display a simple browser window, it suffices completely to create an
	 * instance of the class CefBrowser and to assign its UI component to your
	 * application (e.g. to your content pane). But to be more verbose, this
	 * CTOR keeps an instance of each object on the way to the browser UI.
	 */
	private TestJcef(String startURL, boolean useOSR, boolean isTransparent)
	{
		// (1) The entry point to JCEF is always the class CefApp. There is only
		// one instance per application and therefore you have to call the
		// method "getInstance()" instead of a CTOR.
		//
		// CefApp is responsible for the global CEF context. It loads all
		// required native libraries, initializes CEF accordingly, starts a
		// background task to handle CEF's message loop and takes care of
		// shutting down CEF after disposing it.
		CefApp.addAppHandler(new CefAppHandlerAdapter(null) {
			@Override
			public void stateHasChanged(org.cef.CefApp.CefAppState state)
			{
				// Shutdown the app if the native CEF part is terminated
				if (state == CefAppState.TERMINATED) {
					System.exit(0);
				}
			}
		});
		CefSettings settings = new CefSettings();
		settings.windowless_rendering_enabled = useOSR;
		cefApp = CefApp.getInstance(new String[] { "-disable-gpu=1" },
				settings);

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
		address.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				browser.loadURL(address.getText());
			}
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

		// (5) All UI components are assigned to the default content pane of
		// this JFrame and afterwards the frame is made visible to the user.
		getContentPane().add(address, BorderLayout.NORTH);
		getContentPane().add(browerUI, BorderLayout.CENTER);
		pack();
		setSize(800, 600);
		setVisible(true);

		// (6) To take care of shutting down CEF accordingly, it's important to
		// call the method "dispose()" of the CefApp instance if the Java
		// application will be closed. Otherwise you'll get asserts from CEF.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				CefApp.getInstance().dispose();
				dispose();
			}
		});
	}

	public static void main(String[] args)
	{
		// Perform startup initialization on platforms that require it.
		if (!CefApp.startup(args)) {
			System.out.println("Startup initialization failed!");
			return;
		}

		// The simple example application is created as anonymous class and
		// points to Google as the very first loaded page. Windowed rendering
		// mode is used by default. If you want to test OSR mode set |useOsr| to
		// true and recompile.
		boolean useOsr = false;
		new TestJcef("http://www.google.com", useOsr, false);
	}
}
