package me.silviogames.ld42.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import me.silviogames.ld42.Main;

public class DesktopLauncher
{
    public static void main( String[] arg )
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration( );
        config.resizable = false;
        config.x = 500;
        config.y = 100;
        config.width = Main.world_width * Main.scale;
        config.height = Main.world_height * Main.scale;
        config.vSyncEnabled = true;
        config.title = "warehouse_manager";
        config.addIcon( "icons/app_icon32.png", Files.FileType.Internal );
        config.addIcon( "icons/app_icon128.png", Files.FileType.Internal );
        config.addIcon( "icons/app_icon16.png", Files.FileType.Internal );

        new LwjglApplication( new Main( ), config );
    }
}
