package me.silviogames.ld42.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import me.silviogames.ld42.GameLog;
import me.silviogames.ld42.ID_Type;
import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.states.State_game;

public abstract class Entity
{
    public final Entity_Type type;
    public final int id;
    protected final State_game game;
    float x, y, dx, dy, after_x, after_y, no_y_fighting = MathUtils.random( 0f, 1f ) / 100000f;
    int next_tilex, next_tiley;

    public Entity( State_game game, Entity_Type type, int tilex, int tiley )
    {
        this.game = game;
        this.x = tilex * 6 + 3;
        this.y = tiley * 6 + 3;

        this.id = Main.id_handler.new_ID( ID_Type.ENTITY );

        if ( type == null )
        {
            GameLog.error( "[ENTITY] entity type is null!" );
        }
        this.type = type;
    }

    public void update_main( float delta )
    {
        update( delta );

        //movement logic here
        if ( !type.static_object )
        {
            if ( dx != 0 || dy != 0 )
            {
                after_x = this.x + dx * type.walk_speed * delta * speed( );
                after_y = this.y + dy * type.walk_speed * delta * speed( );

                next_tilex = game.to_tile( after_x );
                next_tiley = game.to_tile( after_y );

                boolean both = game.warehouse.walk( next_tilex, next_tiley );
                boolean xdir = game.warehouse.walk( next_tilex, tiley( ) );
                boolean ydir = game.warehouse.walk( tilex( ), next_tiley );

                if ( both )
                {
                    this.x = after_x;
                    this.y = after_y;
                } else
                {
                    if ( xdir )
                    {
                        this.x = after_x;
                    } else if ( ydir )
                    {
                        this.y = after_y;
                    }
                }
            }
        }

    }

    protected abstract void update( float delta );

    public int tilex()
    {
        return ( int ) ( x / 6 );
    }

    public int tiley()
    {
        return ( int ) ( y / 6 );
    }

    public float get_x()
    {
        return x;
    }

    public float get_y()
    {
        return y;
    }

    public abstract void render( float offset_x, float offset_y );

    public void render_main( float offset_x, float offset_y )
    {
        if ( type.render_debug_box )
        {
            Main.batch.setColor( type.debug_box_color );
            Main.batch.draw( Res.pixel, x - ( type.debug_art_width / 2 ) - offset_x, y - offset_y, type.debug_art_width, type.debug_art_height );
            Main.batch.setColor( Color.WHITE );
        }

        render( offset_x, offset_y );
    }

    protected abstract float speed();

    public float y()
    {
        return y + no_y_fighting;
    }
}
