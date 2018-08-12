package me.silviogames.ld42;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.PrintWriter;
import java.io.StringWriter;

import me.silviogames.ld42.states.State_crash;
import me.silviogames.ld42.states.State_mainmenu;

public class Main extends ApplicationAdapter
{
    public static final int world_width = 300, world_height = 200;
    public static Vector2 pointer = new Vector2( 0, 0 );
    public static boolean mousedown = false;
    public static SpriteBatch batch;
    public static int scale = 3;
    public static Id_Manager id_handler = new Id_Manager( );
    private static Vector3 touchPos = new Vector3( );
    private static int debug_offset_x = 0, debug_offset_y = 0, last_value_x = 0, last_value_y = 0; //last values for printing
    public State state;
    OrthographicCamera camera;
    Viewport viewport;
    private boolean debug_shift_move = false;

    public static int distance( float x1, float y1, float x2, float y2 )
    {
        return ( int ) Math.sqrt( Math.pow( x1 - x2, 2 ) + Math.pow( y1 - y2, 2 ) );
    }

    public static int distance( int x1, int y1, int x2, int y2 )
    {
        return ( int ) Math.sqrt( Math.pow( x1 - x2, 2 ) + Math.pow( y1 - y2, 2 ) );
    }

    public static void draw_box( int x, int y, int width, int height, Color color )
    {
        batch.setColor( color );
        batch.draw( Res.pixel, x, y, width, 1 );
        batch.draw( Res.pixel, x, y + height - 1, width, 1 );
        batch.draw( Res.pixel, x + width - 1, y + 1, 1, height - 2 );
        batch.draw( Res.pixel, x, y + 1, 1, height - 2 );
        batch.setColor( Color.WHITE );
    }

    public static int offset_x( int origin_x )
    {
        last_value_x = origin_x + debug_offset_x;
        return origin_x + debug_offset_x;
    }

    public static int offset_y( int origin_y )
    {
        last_value_y = origin_y + debug_offset_y;
        return origin_y + debug_offset_y;
    }

    public static long xinty( int x, int y )
    {
        return ( ( ( long ) x ) << 32 ) | ( y & 0xffffffffL );
    }

    public static void line( int x1, int y1, int x2, int y2, Color color )
    {
        // delta of exact value and rounded value of the dependent variable
        batch.setColor( color );
        int d = 0;

        int dx = Math.abs( x2 - x1 );
        int dy = Math.abs( y2 - y1 );

        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point

        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        int x = x1;
        int y = y1;

        if ( dx >= dy )
        {
            while ( true )
            {
                batch.draw( Res.pixel, x, y );
                if ( x == x2 )
                    break;
                x += ix;
                d += dy2;
                if ( d > dx )
                {
                    y += iy;
                    d -= dx2;
                }
            }
        } else
        {
            while ( true )
            {
                batch.draw( Res.pixel, x, y );
                if ( y == y2 )
                    break;
                y += iy;
                d += dx2;
                if ( d > dy )
                {
                    x += ix;
                    d -= dy2;
                }
            }
        }
        batch.setColor( Color.WHITE );
    }

    @Override
    public void create()
    {
        batch = new SpriteBatch( );
        camera = new OrthographicCamera( );
        viewport = new FitViewport( world_width, world_height, camera );

        Res.load( );
        init( );


        state = new State_mainmenu( this );
        Res.music.play( );
    }

    @Override
    public void resize( int width, int height )
    {
        viewport.update( width, height, true );
    }

    @Override
    public void render()
    {

        float d = Gdx.graphics.getDeltaTime( );
        update( d > 0.1f ? 0.1f : d );


        Gdx.gl.glClearColor( 0.2f, 0.2f, 0.3f, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT );
        camera.update( );
        batch.setProjectionMatrix( camera.combined );
        batch.begin( );

        try
        {
            state.r( );

            Text.render( batch );

            state.r2( );

            Text.render( batch );

        } catch ( Exception e )
        {
            //// TODO: 11.08.2018 pause music

            String cause = "";
            StringWriter sw = new StringWriter( );
            e.printStackTrace( new PrintWriter( sw ) );
            cause = sw.toString( );
            System.out.println( cause );
            if ( cause.equals( "" ) ) cause = "no crash data available";
            state = new State_crash( this, true, cause );
        }


        batch.end( );
    }

    @Override
    public void dispose()
    {
        batch.dispose( );
        Text.dispose( );
        state.dispose( );
    }

    private void update( float delta )
    {
        touchPos.x = Gdx.input.getX( );
        touchPos.y = Gdx.input.getY( );
        viewport.unproject( touchPos );
        pointer.x = touchPos.x;
        pointer.y = touchPos.y;

        mousedown = Gdx.input.isTouched( 0 );

        //DEBUG move debug offset position to temporarily move sprites while the game is running
        if ( Gdx.input.isKeyJustPressed( Input.Keys.SHIFT_LEFT ) )
        {
            debug_shift_move = !debug_shift_move;
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.UP ) )
        {
            Res.volume++;
            if ( Res.volume >= 10 ) Res.volume = 10;
            if ( !Res.music.isPlaying( ) ) Res.Sounds.WEIRD.play( );
            Res.music.setVolume( Res.volume / 10f );
        } else if ( Gdx.input.isKeyJustPressed( Input.Keys.DOWN ) )
        {
            Res.volume--;
            if ( Res.volume <= 0 ) Res.volume = 0;
            if ( !Res.music.isPlaying( ) ) Res.Sounds.WEIRD.play( );
            Res.music.setVolume( Res.volume / 10f );
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.M ) )
        {
            if ( Res.music.isPlaying( ) )
            {
                Res.music.pause( );
            } else
            {
                Res.music.play( );
            }
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_2 ) )
        {
            debug_offset_y -= ( debug_shift_move ? 10 : 1 );
        }
        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_8 ) )
        {
            debug_offset_y += ( debug_shift_move ? 10 : 1 );
        }
        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_4 ) )
        {
            debug_offset_x -= ( debug_shift_move ? 10 : 1 );
        }
        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_6 ) )
        {
            debug_offset_x += ( debug_shift_move ? 10 : 1 );
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_5 ) )
        {
            System.out.println( "[DEBUG] current offset absolute values " + last_value_x + " " + last_value_y );
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_7 ) )
        {
            debug_offset_x = 0;
            debug_offset_y = 0;
            System.out.println( "[DEBUG] reset offset values" );
        }

        try
        {
            state.u( delta );
        } catch ( Exception e )
        {
            //// TODO: 11.08.2018 stop music

            String cause = "";
            StringWriter sw = new StringWriter( );
            e.printStackTrace( new PrintWriter( sw ) );
            cause = sw.toString( );
            System.out.println( cause );
            if ( cause.equals( "" ) ) cause = "no crash data available";
            state = new State_crash( this, false, cause );
        }

        if ( Gdx.input.isKeyJustPressed( Input.Keys.ESCAPE ) )
        {
            Gdx.app.exit( );
        }
    }

    private void init()
    {
        Text.init( );
    }

    public void change_state( State next_state )
    {
        Res.Sounds.WEIRD.play( );
        state.dispose( );
        mousedown = false;
        this.state = next_state;
    }

}
