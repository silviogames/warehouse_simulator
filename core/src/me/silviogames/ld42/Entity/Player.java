package me.silviogames.ld42.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.Timer;
import me.silviogames.ld42.Values;
import me.silviogames.ld42.states.State_game;

public class Player extends Entity
{

    public Box holding = null;
    boolean l, u, r, d, run;
    int walk_frame = 0;
    private Timer idle = new Timer( 0.3f ), timer_walk_Frame = new Timer( 0.1f );
    private boolean idle_jump = false;


    public Player( State_game game, int tilex, int tiley )
    {
        super( game, Entity_Type.HUMAN, tilex, tiley );
    }

    @Override
    protected void update( float delta )
    {
        if ( idle.update( delta ) )
        {
            idle_jump = !idle_jump;
        }

        if ( timer_walk_Frame.update( delta ) )
        {
            walk_frame++;
            if ( walk_frame > 2 )
            {
                walk_frame = 0;
            }
        }

        dx = 0;
        dy = 0;

        movement( );

        if ( Gdx.input.isKeyJustPressed( Input.Keys.E ) )
        {
            if ( holding == null )
            {
                if ( game.near_box != null && Main.distance( x, y, game.near_box.x, game.near_box.y ) < Values.box_pick_up_distance )
                {
                    if ( game.warehouse.pickup_box( game.near_box.tilex( ), game.near_box.tiley( ), game.near_box.id ) )
                    {
                        holding = game.near_box;
                        game.near_box.status = Box_Status.CARRIED;
                        game.tutorial_pick_up = false;
                    }
                }
            } else
            {
                GridPoint2 place_spot = game.warehouse.get_placement_spot( x, y );
                if ( place_spot != null )
                {
                    if ( holding.place( place_spot.x, place_spot.y ) )
                    {
                        holding = null;
                        game.tutorial_place = false;
                    } else
                    {
                        Res.Sounds.CANT_PLACE.play( );
                    }
                } else
                {
                    Res.Sounds.CANT_PLACE.play( );
                }
            }
        }
    }

    @Override
    public void render( float offset_x, float offset_y )
    {
        //nothing to render here now
        //Text.add( "[tilex] " + tilex( ), x - offset_x, y - offset_y + 30 );
        //Text.add( "[tiley] " + tiley( ), x - offset_x, y - offset_y + 20 );

        //render holding box

        int frame = 0;
        if ( dx == 0 && dy == 0 )
        {
            if ( holding != null )
            {
                frame = 5;
            } else
            {
                if ( idle_jump )
                {
                    frame = 0;
                } else
                {
                    frame = 1;
                }
            }
        } else
        {
            if ( holding != null )
            {
                frame = 5 + walk_frame;
            } else
            {
                frame = 2 + walk_frame;
            }
        }

        Main.batch.draw( Res.human[ frame ], x - offset_x - 3, y - offset_y );

        if ( holding != null )
        {
            Main.batch.draw( Res.boxes[ holding.type.ordinal( ) ], x - offset_x - 3, y - offset_y + 10 );
            //Text.add( "" + holding.id + " " + holding.status.short_name, x - offset_x, y - offset_y + 20, false, 1f );
        }
    }

    @Override
    protected float speed()
    {
        return ( run ? 1.5f : 1 );
    }

    public void reset_position()
    {
        //getting called after week is over
        x = 7 * 6;
        y = 2 * 6;
    }

    private void movement()
    {
        l = Gdx.input.isKeyPressed( Input.Keys.A );
        u = Gdx.input.isKeyPressed( Input.Keys.W );
        d = Gdx.input.isKeyPressed( Input.Keys.S );
        r = Gdx.input.isKeyPressed( Input.Keys.D );

        if ( l || u || d || r ) game.tutorial_walk = false;

        run = Gdx.input.isKeyPressed( Input.Keys.SHIFT_LEFT );

        int sumbool = sum_bool( );
        if ( sumbool < 3 )
        {
            if ( u )
            {
                if ( d )
                {
                    //no movement


                } else if ( l )
                {
                    //diagonal up left
                    dx = -Values.diagonal_delta;
                    dy = Values.diagonal_delta;


                } else if ( r )
                {
                    //diagonal up right
                    dx = Values.diagonal_delta;
                    dy = Values.diagonal_delta;


                } else
                {
                    //only up
                    dy = 1;


                }
            } else if ( r )
            {
                if ( l )
                {
                    //no movement
                } else if ( u )
                {
                    //diagonal up right
                    dx = Values.diagonal_delta;
                    dy = Values.diagonal_delta;

                } else if ( d )
                {
                    //diagonal down right
                    dx = Values.diagonal_delta;
                    dy = -Values.diagonal_delta;

                } else
                {
                    //only right
                    dx = 1;


                }
            } else if ( d )
            {
                if ( u )
                {
                    //no movement
                } else if ( l )
                {
                    //diagonal down left
                    dx = -Values.diagonal_delta;
                    dy = -Values.diagonal_delta;


                } else if ( r )
                {
                    //diagonal down right
                    dx = Values.diagonal_delta;
                    dy = -Values.diagonal_delta;


                } else
                {
                    //only down
                    dy = -1;


                }
            } else if ( l )
            {
                if ( r )
                {
                    //no movement
                } else if ( u )
                {
                    //diagonal up left
                    dx = -Values.diagonal_delta;
                    dy = Values.diagonal_delta;


                } else if ( d )
                {
                    //diagonal down left
                    dx = Values.diagonal_delta;
                    dy = -Values.diagonal_delta;


                } else
                {
                    //only left
                    dx = -1;


                }
            }
        }
    }

    private int sum_bool()
    {
        return ( l ? 1 : 0 ) + ( r ? 1 : 0 ) + ( u ? 1 : 0 ) + ( d ? 1 : 0 );
    }

}
