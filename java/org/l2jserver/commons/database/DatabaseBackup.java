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
package org.l2jserver.commons.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.l2jserver.Config;
import org.l2jserver.commons.enums.ServerMode;

/**
 * @author Mobius
 */
public class DatabaseBackup
{
	public static void performBackup()
	{
		// Delete old files.
		if (Config.BACKUP_DAYS > 0)
		{
			final long cut = LocalDateTime.now().minusDays(Config.BACKUP_DAYS).toEpochSecond(ZoneOffset.UTC);
			final Path path = Paths.get(Config.BACKUP_PATH);
			try
			{
				Files.list(path).filter(n ->
				{
					try
					{
						return Files.getLastModifiedTime(n).to(TimeUnit.SECONDS) < cut;
					}
					catch (Exception ex)
					{
						return false;
					}
				}).forEach(n ->
				{
					try
					{
						Files.delete(n);
					}
					catch (Exception ex)
					{
						// Ignore.
					}
				});
			}
			catch (Exception e)
			{
				// Ignore.
			}
		}
		
		// Dump to file.
		final String mysqldumpPath = System.getProperty("os.name").toLowerCase().contains("win") ? Config.MYSQL_BIN_PATH : "";
		try
		{
			final Process process = Runtime.getRuntime().exec(mysqldumpPath + "mysqldump -u " + Config.DATABASE_LOGIN + (Config.DATABASE_PASSWORD.trim().isEmpty() ? "" : " -p" + Config.DATABASE_PASSWORD) + " " + Config.DATABASE_URL.replace("jdbc:mariadb://", "").replaceAll(".*\\/|\\?.*", "") + " -r " + Config.BACKUP_PATH + (Config.SERVER_MODE == ServerMode.GAME ? "game" : "login") + new SimpleDateFormat("_yyyy_MM_dd_HH_mm'.sql'").format(new Date()));
			process.waitFor();
		}
		catch (Exception e)
		{
			// Ignore.
		}
	}
}
