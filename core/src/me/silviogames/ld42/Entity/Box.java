package me.silviogames.ld42.Entity;

import com.badlogic.gdx.graphics.Color;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.states.State_game;

public class Box extends Entity
{
    private static float highlight_progress = 0f;
    private static boolean highlight_up = true;
    public final Box_Type type;
    public final int code;
    public Box_Status status;
    private float inout_progress = 0f;
    private boolean on_shelf = false;

    public Box( State_game game, int tilex, int tiley, Box_Type type, int code )
    {
        super( game, Entity_Type.BOX, tilex, tiley );
        this.type = type;
        status = Box_Status.IN;
        this.code = code;
    }

    @Override
    protected void update( float delta )
    {

        if ( highlight_up )
        {
            highlight_progress += delta * 1.4f;
            if ( highlight_progress >= 1 )
            {
                highlight_up = false;
                highlight_progress = 1f;
            }
        } else
        {
            highlight_progress -= delta * 1.2f;
            if ( highlight_progress <= 0 )
            {
                highlight_progress = 0;
                highlight_up = true;
            }
        }

        //nothing update
        if ( status == Box_Status.IN )
        {
            inout_progress += delta * 2;
            if ( inout_progress >= 1 )
            {
                inout_progress = 1f;
                status = Box_Status.PLACED;
            }
        } else if ( status == Box_Status.OUT )
        {
            inout_progress -= delta;
            if ( inout_progress <= 0 )
            {
                inout_progress = 0f;
                game.box_is_out( this );
                game.warehouse.box_went_out( tilex( ), tiley( ), id );
                game.remove_entity( this );
                //next frame should not call this update method since at end of every frame entities are removed
            }
        }
    }

    @Override
    public void render( float offset_x, float offset_y )
    {
//        if ( game.near_box != null )
//        {
//            if ( game.near_box.id == this.id && status == Box_Status.PLACED )
//            {
//                //draw border around box
//                if ( Main.distance( x, y, game.player.x, game.player.y ) < Values.box_pick_up_distance && game.player.holding == null )
//                {
//                    Main.batch.setColor( Values.color_tile_highlight );
//                    Main.batch.draw( Res.pixel, x - offset_x - 3 - 1, y - offset_y - 1, 8, 8 );
//                }
//            }
//        }
        boolean big = false;
        if ( game.near_box != null )
        {
            if ( game.near_box.id == this.id && status == Box_Status.PLACED && game.player.holding == null )
            {
                big = true;
            }
        }

        //carried box will stay on position that it has been picked up from until placed so dont render it when carried. player renders the box

        if ( status == Box_Status.CARRIED )
        {
            //draw old position with alpha. //
            Main.batch.setColor( 1, 1, 1, 0f );
        } else
        {
            if ( status == Box_Status.PLACED )
            {
                //  Main.batch.setColor( Color.GOLD );
            } else
            {
                Main.batch.setColor( 1, 1, 1, inout_progress );
            }
        }

        float y_off = 0;
        if ( status == Box_Status.IN || status == Box_Status.OUT )
        {
            y_off = inout_progress * 12f;
            Main.batch.draw( Res.boxes[ type.ordinal( ) ], x - offset_x - 3, y - offset_y + y_off - 12 );
        } else
        {
            if ( big )
            {
                Main.batch.draw( Res.boxes[ type.ordinal( ) ], x - offset_x - ( 3 + highlight_progress ), y - offset_y, 6 + ( 2 * highlight_progress ), 6 + ( 2 * highlight_progress ) );
            } else
            {
                Main.batch.draw( Res.boxes[ type.ordinal( ) ], x - offset_x - 3, y - offset_y );
            }
        }
        Main.batch.setColor( Color.WHITE );
        // Text.add( "" + id + " " + status.short_name, x - offset_x, y - offset_y + 5, false, 0.8f );

    }

    @Override
    protected float speed()
    {
        return type.speed;
    }

    public boolean place( int tilex, int tiley )
    {
        //return true if placed
        if ( game.warehouse.place_box( tilex, tiley, id ) )
        {
            status = Box_Status.PLACED;
            x = tilex * 6 + 3;
            y = tiley * 6 + 3;

            if ( game.warehouse.is_outbox( tilex, tiley ) )
            {
                Res.Sounds.BOX_OUT.play( );
                status = Box_Status.OUT;
                inout_progress = 1f;
            } else
            {
                if ( !on_shelf )
                {
                    game.new_box_on_shelf( this );
                    on_shelf = true;
                }
            }
            return true;
        } else
        {
            return false;
        }
    }
}
