package me.silviogames.ld42.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.State;
import me.silviogames.ld42.Text;
import me.silviogames.ld42.Values;

public class State_mainmenu extends State
{

    public State_mainmenu( Main main )
    {
        super( main );
    }


    @Override
    public void dispose()
    {

    }

    @Override
    protected void update( float delta )
    {
        if ( Gdx.input.isKeyJustPressed( Input.Keys.TAB ) )
        {
            Main.scale += 1;
            if ( Main.scale >= 5 ) Main.scale = 1;

            Gdx.graphics.setWindowedMode( Main.world_width * Main.scale, Main.world_height * Main.scale );
        }


        if ( Gdx.input.isKeyJustPressed( Input.Keys.ENTER ) )
        {
            main.change_state( new State_game( main ) );
            return;
        }


    }

    @Override
    protected void render()
    {

        Text.add( "warehouse_manager", Main.world_width / 2, 142, true, 3f );

        Text.add( "press enter to start game", Main.world_width / 2, Main.world_height / 2, true, 1.2f );

        Text.add( "press tab to resize", Main.world_width / 2, 7, true );

        Text.add( "volume", 20, 7 );
        Text.add( "[UP] and [DOWN]", 7, 22);

        Text.add( "[M] to toggle music", Main.world_width - 90, 7 );

        for ( int i = 0; i < 10; i++ )
        {
            Main.batch.setColor( Values.color_ui_front2 );
            Main.batch.draw( Res.pixel, 19 + ( i * 3 ), 15, 2, 4 );
            Main.batch.setColor( Values.color_ui_front );
            if ( Res.volume < i )
            {
                Main.batch.draw( Res.pixel, 19 + ( i * 3 ), 15, 2, 4 );
            }
            Main.batch.setColor( Color.WHITE );

        }
    }

    @Override
    protected void render2()
    {

    }
}
