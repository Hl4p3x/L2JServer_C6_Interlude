/*
 * This file is part of the L2JServer project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jserver.tools.dbinstaller.gui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.l2jserver.commons.util.SplashScreen;
import org.l2jserver.tools.dbinstaller.RunTasks;
import org.l2jserver.tools.dbinstaller.util.mysql.MySqlConnect;
import org.l2jserver.tools.dbinstaller.util.swing.SpringUtilities;

/**
 * @author mrTJO
 */
public class DBConfigGUI extends JFrame
{
	JTextField _dbHost;
	JTextField _dbPort;
	JTextField _dbUser;
	JPasswordField _dbPass;
	JTextField _dbDbse;
	String _db;
	String _dir;
	Preferences _prop;
	boolean _isVisible = true;
	
	public DBConfigGUI(String db, String dir)
	{
		super("Mobius - DB Installer");
		setVisible(false);
		setLayout(new SpringLayout());
		setDefaultLookAndFeelDecorated(true);
		
		// Set icons.
		final List<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "L2jServer_16x16.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "L2jServer_32x32.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "L2jServer_64x64.png").getImage());
		icons.add(new ImageIcon(".." + File.separator + "images" + File.separator + "L2jServer_128x128.png").getImage());
		setIconImages(icons);
		
		// Show SplashScreen.
		new SplashScreen(".." + File.separator + "images" + File.separator + "splash.png", 5000, this);
		_db = db;
		_dir = dir;
		
		final int width = 260;
		final int height = 220;
		final Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds((resolution.width - width) / 2, (resolution.height - height) / 2, width, height);
		setResizable(false);
		
		_prop = Preferences.userRoot();
		
		// Host
		final JLabel labelDbHost = new JLabel("Host: ", SwingConstants.LEFT);
		add(labelDbHost);
		_dbHost = new JTextField(15);
		_dbHost.setText(_prop.get("dbHost_" + db, "localhost"));
		labelDbHost.setLabelFor(_dbHost);
		add(_dbHost);
		
		// Port
		final JLabel labelDbPort = new JLabel("Port: ", SwingConstants.LEFT);
		add(labelDbPort);
		_dbPort = new JTextField(15);
		_dbPort.setText(_prop.get("dbPort_" + db, "3306"));
		labelDbPort.setLabelFor(_dbPort);
		add(_dbPort);
		
		// Username
		final JLabel labelDbUser = new JLabel("Username: ", SwingConstants.LEFT);
		add(labelDbUser);
		_dbUser = new JTextField(15);
		_dbUser.setText(_prop.get("dbUser_" + db, "root"));
		labelDbUser.setLabelFor(_dbUser);
		add(_dbUser);
		
		// Password
		final JLabel labelDbPass = new JLabel("Password: ", SwingConstants.LEFT);
		add(labelDbPass);
		_dbPass = new JPasswordField(15);
		_dbPass.setText(_prop.get("dbPass_" + db, ""));
		labelDbPass.setLabelFor(_dbPass);
		add(_dbPass);
		
		// Database
		final JLabel labelDbDbse = new JLabel("Database: ", SwingConstants.LEFT);
		add(labelDbDbse);
		_dbDbse = new JTextField(15);
		_dbDbse.setText(_prop.get("dbDbse_" + db, db));
		labelDbDbse.setLabelFor(_dbDbse);
		add(_dbDbse);
		
		final ActionListener cancelListener = e -> System.exit(0);
		
		// Cancel
		final JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(cancelListener);
		add(btnCancel);
		
		final ActionListener connectListener = e ->
		{
			MySqlConnect connector = null;
			try
			{
				connector = new MySqlConnect(_dbHost.getText(), _dbPort.getText(), _dbUser.getText(), new String(_dbPass.getPassword()), _dbDbse.getText(), false);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			if ((connector != null) && (connector.getConnection() != null))
			{
				_prop.put("dbHost_" + _db, _dbHost.getText());
				_prop.put("dbPort_" + _db, _dbPort.getText());
				_prop.put("dbUser_" + _db, _dbUser.getText());
				_prop.put("dbDbse_" + _db, _dbDbse.getText());
				
				final DBInstallerGUI dbi = new DBInstallerGUI(connector.getConnection());
				setVisible(false);
				
				if (_dir.equals("sql/login/"))
				{
					final Object[] options =
					{
						"Install Login",
						"Exit"
					};
					final int n = JOptionPane.showOptionDialog(null, "Install login server database?", "Select an option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if ((n == 1) || (n == -1))
					{
						System.exit(0);
					}
				}
				else
				{
					final Object[] options =
					{
						"Install Server",
						"Exit"
					};
					final int n = JOptionPane.showOptionDialog(null, "Install game server database?", "Select an option", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					if ((n == 1) || (n == -1))
					{
						System.exit(0);
					}
				}
				_isVisible = false;
				dbi.setVisible(true);
				
				final RunTasks task = new RunTasks(dbi, _db, _dir);
				task.setPriority(Thread.MAX_PRIORITY);
				task.start();
			}
		};
		
		// Connect
		final JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(connectListener);
		add(btnConnect);
		
		SpringUtilities.makeCompactGrid(getContentPane(), 6, 2, 5, 5, 5, 5);
		pack();
	}
	
	@Override
	public boolean isVisible()
	{
		return _isVisible;
	}
}
