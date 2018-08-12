package me.silviogames.ld42.Entity;


import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntSet;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.Timer;
import me.silviogames.ld42.Values;
import me.silviogames.ld42.states.Path;
import me.silviogames.ld42.states.State_game;

public class Robot extends Entity
{

    public Box holding;
    int[] path_x, path_y;
    int path_index = -1, path_id = -1, target_box_id = -1;
    int target_tilex = -1, target_tiley = -1;
    int target_pos_x, target_pos_y;
    private TASK current_task = TASK.IDLE, next_task = TASK.IDLE;
    private Timer timer_walker = new Timer( 0.1f ), timer_wait_box = new Timer( 2f );
    private Timer timer_idle = new Timer( 0.07f );
    private Timer timer_scane = new Timer( 1f ), timer_memory = new Timer( 60 );

    private IntSet known_box_ids = new IntSet( );

    public Robot( State_game game, int tilex, int tiley )
    {
        super( game, Entity_Type.ROBOT, tilex, tiley );


    }


    @Override
    protected void update( float delta )
    {
        switch ( current_task )
        {
            case IDLE:

                if ( timer_idle.update( delta ) )
                {
                    //look if any known box is requested
                    for ( int i = 0; i < game.requests.size; i++ )
                    {
                        int requested_box_id = game.requests.get( i );
                        Box b = game.get_box( requested_box_id );
                        if ( b == null )
                        {
                            System.out.println( "that requested box is not here anymore? should not be so" );
                        } else
                        {
                            if ( known_box_ids.contains( requested_box_id ) && game.warehouse.claim_spot( b.tilex( ), b.tiley( ) ) )
                            {
                                //go pick up that box
                                current_task = TASK.FIND_PATH;
                                path_id = game.search_path( tilex( ), tiley( ), b.tilex( ), b.tiley( ) );
                                if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                                target_tilex = b.tilex( );
                                target_tiley = b.tiley( );
                                target_box_id = requested_box_id;
                                next_task = TASK.PICK_UP_BOX;
                                //spot has been claimed! unclaim later!
                                return;
                            }
                        }
                    }

                    //scan boxes randomly. memory is overwritten
                    if ( known_box_ids.size < Values.robot_memory_size )
                    {
                        GridPoint3 box_on_shelf = game.warehouse.get_random_filled_shelfpos( );
                        if ( box_on_shelf != null && !known_box_ids.contains( box_on_shelf.z ) )
                        {
                            current_task = TASK.FIND_PATH;
                            path_id = game.search_path( tilex( ), tiley( ), box_on_shelf.x, box_on_shelf.y );
                            if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                            target_tilex = box_on_shelf.x;
                            target_tiley = box_on_shelf.y;
                            current_task = TASK.FIND_PATH;
                            target_box_id = box_on_shelf.z;
                            next_task = TASK.SCAN_BOX;
                            return;
                        }
                    }

                    //take box from inbox

                    GridPoint3 inbox = game.warehouse.get_filled_inbox_position( );
                    if ( inbox != null && game.warehouse.claim_spot( inbox.x, inbox.y ) )
                    {
                        //walk to inbox
                        current_task = TASK.FIND_PATH;
                        path_id = game.search_path( tilex( ), tiley( ), inbox.x, inbox.y );
                        if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                        target_tilex = inbox.x;
                        target_tiley = inbox.y;
                        target_box_id = inbox.z;
                        next_task = TASK.PICK_UP_BOX;
                        //spot has been claimed! unclaim later!

                    }
                }
                break;
            case FIND_PATH:
                if ( Path.result( path_id ) == Path.Result.DONE )
                {
                    path_x = Path.get_x_path( path_id );
                    path_y = Path.get_y_path( path_id );
                    game.garbage_path( path_id );
                    path_id = -1;
                    current_task = TASK.WALK;
                    path_index = 0;
                    go( );
                } else if ( Path.result( path_id ) == Path.Result.FAIL )
                {
                    game.garbage_path( path_id );
                    path_id = -1;
                    game.warehouse.unclaim( target_tilex, target_tiley );
                    target_box_id = -1;
                    target_tilex = -1;
                    target_tiley = -1;
                    current_task = TASK.IDLE;
                    next_task = TASK.IDLE;
                }
                break;

            case WALK:
                if ( timer_walker.update( delta ) )
                {
                    go( );
                }
                if ( Main.distance( x, y, target_pos_x, target_pos_y ) < 2 )
                {
                    dx = 0;
                    dy = 0;
                    path_index++;
                    if ( path_index < path_x.length )
                    {
                        go( );
                    } else
                    {
                        //robot is at end of path
                        current_task = next_task;
                        next_task = TASK.IDLE;
                    }
                }
                break;
            case PICK_UP_BOX:
                //assuming that near box
                if ( game.warehouse.pickup_box( target_tilex, target_tiley, target_box_id ) )
                {
                    game.warehouse.unclaim( target_tilex, target_tiley );
                    holding = game.get_box( target_box_id );
                    if ( holding == null )
                    {
                        System.out.println( "ROBOT COULD NOT PICK UP BOX WITH ID " + target_box_id );
                        current_task = TASK.IDLE;
                        next_task = TASK.IDLE;
                    } else
                    {
                        holding.status = Box_Status.CARRIED;

                        //look if this box is requested
                        if ( game.requests.contains( holding.id ) )
                        {
                            GridPoint2 spot_outbox = game.warehouse.get_free_outbox_spot( );
                            if ( spot_outbox != null && game.warehouse.claim_spot( spot_outbox.x, spot_outbox.y ) )
                            {
                                current_task = TASK.FIND_PATH;
                                path_id = game.search_path( tilex( ), tiley( ), spot_outbox.x, spot_outbox.y );
                                if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                                target_tilex = spot_outbox.x;
                                target_tiley = spot_outbox.y;
                                next_task = TASK.PLACE_BOX;
                                return;
                            } else
                            {
                                System.out.println( "no outbox slot is free. waiting with box" );
                                current_task = TASK.WAIT_WITH_BOX;
                                next_task = TASK.IDLE;
                                return;
                            }
                        }

                        //not requested so place box on shelf
                        GridPoint2 shelf_pos = game.warehouse.get_free_shelf_spot( );

                        if ( shelf_pos != null && game.warehouse.claim_spot( shelf_pos.x, shelf_pos.y ) )
                        {
                            current_task = TASK.FIND_PATH;
                            path_id = game.search_path( tilex( ), tiley( ), shelf_pos.x, shelf_pos.y );
                            if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                            target_tilex = shelf_pos.x;
                            target_tiley = shelf_pos.y;
                            next_task = TASK.PLACE_BOX;
                        } else
                        {
                            //cant find free shelf yet
                            current_task = TASK.WAIT_WITH_BOX;
                            next_task = TASK.IDLE;
                        }
                    }
                } else
                {
                    System.out.println( "box is not here anymore? or this is other box" );
                    //player must have taken the box
                    game.warehouse.unclaim( target_tilex, target_tiley );
                    current_task = TASK.IDLE;
                    next_task = TASK.IDLE;
                }
                break;
            case PLACE_BOX:
                if ( holding == null )
                {
                    current_task = TASK.IDLE;
                    next_task = TASK.IDLE;

                    game.warehouse.unclaim( target_tilex, target_tiley );
                    target_box_id = -1;
                    target_tilex = -1;
                    target_tiley = -1;
                } else
                {
                    if ( holding.place( target_tilex, target_tiley ) )
                    {
                        if ( known_box_ids.contains( holding.id ) )
                        {
                            known_box_ids.remove( holding.id );
                        }
                        holding = null;
                        game.warehouse.unclaim( target_tilex, target_tiley );
                        target_box_id = -1;
                        target_tilex = -1;
                        target_tiley = -1;

                    } else
                    {
                        game.warehouse.unclaim( target_tilex, target_tiley );
                        target_box_id = -1;
                        target_tilex = -1;
                        target_tiley = -1;
                        current_task = TASK.WAIT_WITH_BOX;
                        next_task = TASK.IDLE;
                    }
                }
                break;
            case WAIT_WITH_BOX:
                //regularly look for new free shelf position or bring box directly to outbox if requested!
                if ( timer_wait_box.update( delta ) )
                {
                    //see if this box is requested
                    if ( game.requests.contains( holding.id ) )
                    {
                        GridPoint2 spot_outbox = game.warehouse.get_free_outbox_spot( );
                        if ( spot_outbox != null && game.warehouse.claim_spot( spot_outbox.x, spot_outbox.y ) )
                        {
                            current_task = TASK.FIND_PATH;
                            path_id = game.search_path( tilex( ), tiley( ), spot_outbox.x, spot_outbox.y );
                            if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                            target_tilex = spot_outbox.x;
                            target_tiley = spot_outbox.y;
                            next_task = TASK.PLACE_BOX;
                        } else
                        {
                            System.out.println( "no outbox slot is free. waiting with box" );
                            current_task = TASK.WAIT_WITH_BOX;
                            next_task = TASK.IDLE;
                            return;
                        }
                    }

                    //place box on shelf
                    GridPoint2 shelf_pos = game.warehouse.get_free_shelf_spot( );
                    if ( shelf_pos == null )
                    {
                        //cant find free shelf yet
                        current_task = TASK.WAIT_WITH_BOX;
                        next_task = TASK.IDLE;
                    } else
                    {
                        current_task = TASK.FIND_PATH;
                        path_id = game.search_path( tilex( ), tiley( ), shelf_pos.x, shelf_pos.y );
                        if ( path_id < 0 ) System.out.println( "ROBOT PATH ERROR!" );
                        target_tilex = shelf_pos.x;
                        target_tiley = shelf_pos.y;
                        next_task = TASK.PLACE_BOX;
                    }
                }
                break;
            case SCAN_BOX:
                if ( game.warehouse.is_box_here( target_tilex, target_tiley, target_box_id ) )
                {
                    if ( timer_scane.update( delta ) )
                    {
                        Res.Sounds.SCAN.play( );
                        if ( known_box_ids.size < Values.robot_memory_size )
                        {
                            System.out.println( "scanned box " + target_box_id );
                            known_box_ids.add( target_box_id );
                        } else
                        {
                            System.out.println( "could not scan box. memory full" );
                        }
                        current_task = TASK.IDLE;
                        next_task = TASK.IDLE;
                    }
                } else
                {
                    timer_scane.reset( );
                }
                break;
        }

    }

    @Override
    public void render( float offset_x, float offset_y )
    {
        int frame = 0;
        if ( dy < 0 )
        {
            //down
            if ( Math.abs( dx ) < 0.1f )
            {
                frame = 0;
            } else
            {
                if ( dx < 0 )
                {
                    frame = 1;
                } else
                {
                    frame = 2;
                }
            }
        } else
        {
            //up
            if ( Math.abs( dx ) < 0.1f )
            {
                frame = 3;
            } else
            {
                if ( dx < 0 )
                {
                    frame = 4;
                } else
                {
                    frame = 5;
                }
            }
        }

        Main.batch.draw( Res.robot[ frame ], x - offset_x - 5, y - offset_y );

        if ( holding != null )
        {
            Main.batch.draw( Res.boxes[ holding.type.ordinal( ) ], x - offset_x - 3, y - offset_y + 10 );
        } else
        {


        }
        //   Text.add( "memory " + known_box_ids.size, x - offset_x, y - offset_y + 20, false, 0.8f );
    }

    @Override
    protected float speed()
    {
        return ( 1f + ( ( ( float ) game.robot_speed_extra_speed ) / 10f ) );
    }

    private int tile_to_path_pos( int tilex )
    {
        return ( tilex * 6 ) + 3;
    }

    public void reset()
    {
        holding = null;
        current_task = TASK.IDLE;
        next_task = TASK.IDLE;
        known_box_ids.clear( );
        dx = 0;
        dy = 0;
        x = ( game.warehouse.width( ) / 2 ) * 6;
        y = 6;
    }

    private void go()
    {
        target_pos_x = tile_to_path_pos( path_x[ path_index ] );
        target_pos_y = tile_to_path_pos( path_y[ path_index ] );
        float angle = MathUtils.atan2( target_pos_y - this.y, target_pos_x - this.x );
        dx = MathUtils.cos( angle );
        dy = MathUtils.sin( angle );
    }

    public enum TASK
    {
        IDLE, FIND_PATH, WALK, PICK_UP_BOX, PLACE_BOX, WAIT_WITH_BOX, SCAN_BOX;
    }

}
