package me.silviogames.ld42.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import me.silviogames.ld42.Button;
import me.silviogames.ld42.Main;
import me.silviogames.ld42.State;
import me.silviogames.ld42.Text;

public class State_crash extends State
{

    Button clip_board;
    private boolean rendering;
    private String crash_cause[];
    private String full_cause;

    public State_crash( Main main, boolean rendering, String crash_cause )
    {
        super( main );
        clip_board = new Button( Main.world_width / 2, 10, 100, 20, "copy to clipboard", true );
        addButton( clip_board );
        if ( crash_cause == null )
        {
            clip_board.hide( );
            crash_cause = "no crash data available";
        }
        full_cause = crash_cause;
        this.crash_cause = Text.wrap( crash_cause, Main.world_width - 20 );


    }

    @Override
    public void dispose()
    {

    }

    @Override
    protected void update( float delta )
    {
        if ( Gdx.input.isKeyJustPressed( Input.Keys.ESCAPE ) )
        {
            main.change_state( new State_mainmenu( main ) );
            return;
        }

        if ( clip_board.pressed( ) )
        {
            Gdx.app.getClipboard( ).setContents( full_cause );
            System.out.println( "copied to clip board" );
        }
    }

    @Override
    protected void render()
    {
        Text.add( "unfortunately the game crashed in the " + ( rendering ? "render" : "update" ) + " loop :(", 10, Main.world_height - 10 );
        Text.add( "sending me this crash message on", 10, Main.world_height - 20 );
        Text.add( "twitter @SilvioMilvio will help me a lot :)", 10, Main.world_height - 30 );


        for ( int i = 0; i < crash_cause.length; i++ )
        {
            Text.add( crash_cause[ i ], 10, Main.world_height - 50 - ( i * 10 ), 0.8f );
        }


    }

    @Override
    protected void render2()
    {

    }
}
