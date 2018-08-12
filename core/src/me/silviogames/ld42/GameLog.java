package me.silviogames.ld42;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Silvio on 12.03.2017.
 */

public class GameLog
{
    private static Array< String > content = new Array< String >( );

    public GameLog()
    {

    }

    public static void error( String message )
    {
        log( message, LogType.ERROR );
    }

    public static void debug( String message )
    {
        log( message, LogType.DEBUG );
    }

    public static void info( String message )
    {
        log( message, LogType.INFO );
    }

    public static void start_end( String message )
    {
        log( message, LogType.START_STOP );
    }

    private static void log( String message, LogType type )
    {
        //System.out.println( message );
        //content.add( "<p id = \"" + type.id + "\">" + message + " </p>" );
        //if ( content.size > 1000 ) flush( );
    }

    public static void flush()
    {
        System.out.println( "[GAMELOG] FLUSHING. MIGHT STUTTER" );
        //save content to file
        FileHandle file = Gdx.files.local( "log.html" );
        for ( String line : content )
        {
            file.writeString( line, true );
        }
        content.clear( );
    }

    public enum LogType
    {
        ERROR( "error" ),
        INFO( "info" ),
        DEBUG( "debug" ),
        START_STOP( "header" );

        public final String id;

        LogType( String id )
        {
            this.id = id;
        }
    }
}
