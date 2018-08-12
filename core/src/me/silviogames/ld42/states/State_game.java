package me.silviogames.ld42.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectMap;

import me.silviogames.ld42.Button;
import me.silviogames.ld42.Entity.Box;
import me.silviogames.ld42.Entity.Box_Status;
import me.silviogames.ld42.Entity.Box_Type;
import me.silviogames.ld42.Entity.Entity;
import me.silviogames.ld42.Entity.Entity_Type;
import me.silviogames.ld42.Entity.Player;
import me.silviogames.ld42.Entity.Robot;
import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.State;
import me.silviogames.ld42.Text;
import me.silviogames.ld42.Timer;
import me.silviogames.ld42.Values;
import me.silviogames.ld42.warehouse.Days;
import me.silviogames.ld42.warehouse.Shelf;
import me.silviogames.ld42.warehouse.Warehouse;

public class State_game extends State
{

    public int click_tilex = -1, click_tiley = -1;
    public Warehouse warehouse;
    public Box near_box = null;
    public Player player;
    public Days current_day = Days.MONDAY;
    public boolean shelf_vertical = true;
    public MODE mode = MODE.START;
    public boolean tutorial_walk = true;
    public boolean tutorial_pick_up = true, tutorial_place = true, moved_warehouse = false;
    public IntArray requests = new IntArray( ); //ids of boxes that should go out of warehouse
    public int robot_speed_extra_speed = 0;
    public Array< Shelf > list_shelves = new Array< Shelf >( );
    public int possesed_shelves = 1, extra_robot_memory = 0;
    Array< Entity > list_entites = new Array< Entity >( );
    float offset_x = 0, offset_y = 0;
    Array< Box > list_box = new Array< Box >( );
    Array< Robot > list_robot = new Array< Robot >( );
    IntSet remove_set = new IntSet( ); //ids of entities that should be removed
    IntArray box_ids = new IntArray( ); //ids of boxes that still should be in the warehouse, can contain ids of boxes that the player has already moved in outbox ! so not equal to actual boxes in warehouse
    ObjectMap< Integer, Box > box_map = new ObjectMap< Integer, Box >( ); // all boxes in warehouse, boxes should be removed here when box is out of request list. not when out of warehouse!
    ObjectMap< Integer, Timer > request_timer = new ObjectMap< Integer, Timer >( );
    ObjectMap< Integer, Float > request_penalty = new ObjectMap< Integer, Float >( ); // time you have for delivery. becomes negative!
    boolean highlight_reqeusted_box = false; //is true when player is standing next to a requested box and has scanned it.
    String[] wrapped_start_text;
    //buttons
    //mode startweek
    Button start_week, button_next_week, button_shop, button_back_from_shop, button_buy_shelf, button_buy_robot, button_upgrade_robot, button_increase_size;
    //mode upgrade robots
    Button upgrade_speed, upgrade_memory, upgrade_backshop, button_back_from_expand, button_expand;

    //mode place_shelf
    Button back_from_shelf_placing;
    Button button_place_shelf;

    //
    int[] path_x, path_y;
    private Array< Path > list_path = new Array< Path >( );
    private IntArray garbage_paths = new IntArray( );
    private float spectate_offset_x = 0, spectate_offset_y = 0;
    private float warehouse_default_x, warehouse_default_y, blink_time = 0f;
    private boolean lerping_to_center = false, show_help = true, blink = false;
    private boolean y_sort = true, sort_descending = true;
    private Timer timer_day = new Timer( Values.day_length );
    private Timer new_box_timer = new Timer( 1 );
    private int last_scanned_code = -1, money = 0, highlight_box_id = -1;
    private float scanning_progress = 0f;
    private int scanning_id = -1; //see if scanning same box
    private ObjectMap< MODE, Array< Button > > mode_buttons = new ObjectMap< MODE, Array< Button > >( );
    private int path_status = 0; //0 no start, 1 no end, 2 have everything
    private int path_id = -1;

    private Timer timer_not_money = new Timer( 3f );
    private boolean not_money = false;

    private int speed_upgrade_cost = 1000;


    public State_game( Main main )
    {
        super( main );
        player = new Player( this, 7, 2 );
        warehouse = new Warehouse( this );
        add_entity( player );

        for ( MODE m : MODE.values( ) )
        {
            Array< Button > buttons = new Array< Button >( );

            switch ( m )
            {
                case WORKING:
                    //no buttons needed
                    break;
                case WEEKOVER:
                    //next week button
                    button_next_week = new Button( Main.world_width - 50, 80, 80, 20, "start next week", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_next_week );
                    addButton( button_next_week, 1 );
                    //shop button
                    button_shop = new Button( Main.world_width - 50, 50, 80, 20, "shop", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_shop );
                    addButton( button_shop, 1 );
                    break;
                case START:
                    //start button
                    start_week = new Button( Main.world_width - 75, 50, 80, 20, "start week", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( start_week );
                    addButton( start_week, 1 );
                    break;
                case SHOP:
                    button_back_from_shop = new Button( Main.world_width / 2, 6, 80, 20, "back", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_back_from_shop );
                    addButton( button_back_from_shop, 1 );

                    button_buy_robot = new Button( 40, 70, 60, 20, "buy", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_buy_robot );
                    addButton( button_buy_robot, 1 );

                    button_upgrade_robot = new Button( Main.world_width - 50, 70, 60, 20, "upgrade", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_upgrade_robot );
                    addButton( button_upgrade_robot, 1 );

                    button_buy_shelf = new Button( 40, 100, 60, 20, "buy", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_buy_shelf );
                    addButton( button_buy_shelf, 1 );

                    button_place_shelf = new Button( Main.world_width - 50, 100, 60, 20, "place", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_place_shelf );
                    addButton( button_place_shelf, 1 );

                    button_increase_size = new Button( Main.world_width - 150, 130, 100, 20, "expand warehouse", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_increase_size );
                    addButton( button_increase_size, 1 );

                    break;
                case UPGRADE_ROBOTS:
                    upgrade_speed = new Button( 40, 70, 60, 20, "upgrade", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( upgrade_speed );
                    addButton( upgrade_speed, 1 );

                    upgrade_backshop = new Button( Main.world_width / 2, 6, 80, 20, "back", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( upgrade_backshop );
                    addButton( upgrade_backshop, 1 );

                    upgrade_memory = new Button( 40, 100, 60, 20, "upgrade", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( upgrade_memory );
                    addButton( upgrade_memory, 1 );
                    break;

                case PLACE_SHELF:
                    back_from_shelf_placing = new Button( Main.world_width - 50, 50, 80, 20, "back", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( back_from_shelf_placing );
                    addButton( back_from_shelf_placing, 1 );
                    break;

                case EXPAND_WAREHOUSE:

                    button_back_from_expand = new Button( Main.world_width - 50, 40, 80, 20, "back", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_back_from_expand );
                    addButton( button_back_from_expand, 1 );

                    button_expand = new Button( Main.world_width - 50, 70, 80, 20, "expand", Values.color_ui_front, Values.color_ui_back, true );
                    buttons.add( button_expand );
                    addButton( button_expand, 1 );
                    break;
            }

            mode_buttons.put( m, buttons );
            //add buttons individually to set layer
        }


        change_mode( MODE.START );

        wrapped_start_text = Text.wrap( Values.start_text, 140 );


    }

    @Override
    public void dispose()
    {

    }

    @Override
    protected void update( float delta )
    {
        if ( y_sort ) quickSort( list_entites, 0, list_entites.size - 1, sort_descending );

        click_tilex = ( int ) ( offset_x + Main.pointer.x ) / 6;
        click_tiley = ( int ) ( offset_y + Main.pointer.y ) / 6;


        switch ( mode )
        {
            case START:

                offset_x = warehouse_default_x + spectate_offset_x;
                offset_y = warehouse_default_y + spectate_offset_y;

                spectate_warehouse( delta );

                if ( start_week.pressed( ) )
                {
                    change_mode( MODE.WORKING );
                }
                break;
            case WORKING:

                offset_x = player.get_x( ) - ( 100 );
                offset_y = player.get_y( ) - ( 100 );

                calculate_paths( );

                if ( timer_day.update( delta ) )
                {
                    if ( current_day == Days.SUNDAY )
                    {
                        change_mode( MODE.WEEKOVER );
                        warehouse.next_week( );
                        clear_lists( );
                        return;
                    }
                    //next day
                    current_day = current_day.next_day( );
                    //check for sunday monday transition
                }

                //check all boxes if they want be move out of warehouse
                for ( int i = 0; i < box_ids.size; i++ )
                {
                    Timer box_timer = request_timer.get( box_ids.get( i ), null );
                    if ( box_timer == null )
                    {
                        //request is already displayed
                    } else
                    {
                        if ( box_timer.update( delta ) )
                        {
                            //add box id to request list
                            requests.add( box_ids.get( i ) );
                            //remove box id timer
                            request_timer.remove( box_ids.get( i ) );
                            //add delivery timer
                            request_penalty.put( box_ids.get( i ), Values.request_time );
                        }
                    }
                }

                //update request penalty timers
                float t = 0;

                for ( int i = box_ids.size - 1; i >= 0; i-- )
                {
                    if ( request_penalty.containsKey( box_ids.get( i ) ) )
                    {
                        t = request_penalty.get( box_ids.get( i ) );
                        t -= delta;
                        request_penalty.put( box_ids.get( i ), t );
                        if ( t < -Values.request_time )
                        {
                            money -= Values.full_box_delivery_money * 2; //box that has not been delivered gets bigger penalty!
                            if ( money <= 0 ) money = 0;
                            System.out.println( "box not delivered! penalty -" + ( Values.full_box_delivery_money * 2 ) );
                            requests.removeValue( box_ids.get( i ) );
                            box_map.remove( box_ids.get( i ) );
                            box_ids.removeIndex( i );
                        }
                    }
                }

                //add new boxes to warehouse
                if ( new_box_timer.update( delta ) )
                {
                    if ( warehouse.free_inbox_slots( ) > 0 )
                    {
                        // System.out.println( "free slot" );
                        GridPoint2 new_box_spot = warehouse.get_free_box_spot( );
                        if ( new_box_spot != null )
                        {
                            int next_box_code = warehouse.get_unused_box_code( );
                            if ( next_box_code == -9999 )
                            {
                                System.out.println( "no more boxes this week!" );
                            } else
                            {
                                Box new_box = new Box( this, new_box_spot.x, new_box_spot.y, Box_Type.random( ), next_box_code );

                                if ( warehouse.spawn_box( new_box_spot.x, new_box_spot.y, new_box.id ) )
                                {
                                    //should always work since already checked before
                                    add_entity( new_box );
                                    Res.Sounds.NEW_BOX.play( );
                                } else
                                {
                                    System.out.println( "ERROR could not place box :(" );
                                }
                            }
                        } else
                        {
                            //  System.out.println( "no free slot for new box" );
                        }
                    } else
                    {
                        // System.out.println( "no free slots" );
                    }
                    new_box_timer.changeFinalTime( MathUtils.random( 1f, 8f ) );
                }

                near_box = find_nearest_box( );

                highlight_reqeusted_box = false;
                highlight_box_id = -1;
                if ( player.holding == null )
                {
                    if ( near_box != null )
                    {
                        //request box highlighting
                        if ( last_scanned_code == near_box.code )
                        {
                            if ( requests.contains( near_box.id ) )
                            {
                                highlight_reqeusted_box = true;
                                highlight_box_id = near_box.id;
                            }
                        }

                        if ( scanning_id == -1 )
                        {
                            if ( last_scanned_code == near_box.code )
                            {
                                //dont scan this box again
                            } else
                            {
                                if ( Gdx.input.isKeyPressed( Input.Keys.Q ) )
                                {
                                    scanning_id = near_box.id;
                                } else
                                {
                                    scanning_progress = 0;
                                }
                            }
                        } else
                        {
                            if ( near_box.id == scanning_id )
                            {
                                if ( Gdx.input.isKeyPressed( Input.Keys.Q ) )
                                {
                                    scanning_progress += delta;
                                    if ( scanning_progress >= Values.scan_time )
                                    {
                                        Res.Sounds.SCAN.play( );
                                        last_scanned_code = near_box.code;
                                        scanning_id = -1;
                                        scanning_progress = 0;
                                    }
                                } else
                                {
                                    scanning_id = -1;
                                    scanning_progress = 0;
                                }
                            } else
                            {
                                scanning_id = -1;
                                scanning_progress = 0;
                            }
                        }
                    } else
                    {
                        if ( scanning_id != -1 )
                        {
                            scanning_id = -1;
                            scanning_progress = 0f;
                        }
                    }
                }

                if ( player.holding != null )
                {
                    if ( requests.contains( player.holding.id ) && last_scanned_code == player.holding.code )
                    {
                        highlight_reqeusted_box = true;
                        highlight_box_id = player.holding.id;
                    }
                }
                for ( Entity e : list_entites )
                {
                    e.update_main( delta );
                }


                blink_time += delta;
                if ( blink_time > 0.3f )
                {
                    blink = !blink;
                    blink_time = 0f;
                }

                if ( remove_set.size > 0 )
                {
                    for ( int i = list_entites.size - 1; i >= 0; i-- )
                    {
                        if ( remove_set.contains( list_entites.get( i ).id ) )
                        {
                            list_entites.removeIndex( i );
                        }
                    }
                    for ( int i = list_box.size - 1; i >= 0; i-- )
                    {
                        if ( remove_set.contains( list_box.get( i ).id ) )
                        {
                            list_box.removeIndex( i );
                        }
                    }
                    for ( int i = list_robot.size - 1; i >= 0; i-- )
                    {
                        if ( remove_set.contains( list_robot.get( i ).id ) )
                        {
                            list_robot.removeIndex( i );
                        }
                    }

                    remove_set.clear( );
                }
                break;
            case WEEKOVER:

                offset_x = warehouse_default_x + spectate_offset_x;
                offset_y = warehouse_default_y + spectate_offset_y;

                spectate_warehouse( delta );

                if ( button_next_week.pressed( ) )
                {
                    warehouse.next_week( );
                    current_day = Days.MONDAY;
                    timer_day.reset( );
                    change_mode( MODE.WORKING );
                    return;
                }

                if ( button_shop.pressed( ) )
                {
                    change_mode( MODE.SHOP );
                    return;
                }
                break;
            case SHOP:

                if ( not_money )
                {
                    if ( timer_not_money.update( delta ) )
                    {
                        not_money = false;
                    }
                }

                if ( button_back_from_shop.pressed( ) )
                {
                    change_mode( MODE.WEEKOVER );
                    return;
                }

                if ( button_increase_size.pressed( ) )
                {
                    change_mode( MODE.EXPAND_WAREHOUSE );
                    return;
                }

                if ( Gdx.input.isKeyJustPressed( Input.Keys.NUMPAD_9 ) )
                {
                    money += 100000; //// TODO: 12.08.2018 remove when released!
                }

                if ( button_buy_shelf.pressed( ) )
                {
                    if ( money >= Values.shelf_prive )
                    {
                        possesed_shelves++;
                        money -= Values.shelf_prive;
                    } else
                    {
                        not_money = true;
                        Res.Sounds.CANT_PLACE.play( );
                    }
                }

                if ( button_place_shelf.pressed( ) )
                {
                    change_mode( MODE.PLACE_SHELF );
                    return;
                }

                if ( button_buy_robot.pressed( ) )
                {
                    if ( money >= Values.robot_prize )
                    {
                        add_entity( new Robot( this, Values.new_robot_tilex, Values.new_robot_tiley ) );
                        money -= Values.robot_prize;
                        button_upgrade_robot.show( );
                    } else
                    {
                        not_money = true;
                        Res.Sounds.CANT_PLACE.play( );
                    }
                }

                if ( button_upgrade_robot.pressed( ) )
                {
                    change_mode( MODE.UPGRADE_ROBOTS );
                    return;
                }

                break;
            case UPGRADE_ROBOTS:
                if ( not_money )
                {
                    if ( timer_not_money.update( delta ) )
                    {
                        not_money = false;
                    }
                }

                if ( upgrade_backshop.pressed( ) )
                {
                    change_mode( MODE.SHOP );
                    return;
                }

                if ( upgrade_speed.pressed( ) )
                {
                    int speed_upgrade_cost = calc_speed_upgrade_cost( );
                    if ( money >= speed_upgrade_cost )
                    {
                        robot_speed_extra_speed++;
                        money -= speed_upgrade_cost;
                    } else
                    {
                        not_money = true;
                        Res.Sounds.CANT_PLACE.play( );
                    }
                }

                if ( upgrade_memory.pressed( ) )
                {
                    if ( money >= Values.memory_price )
                    {
                        extra_robot_memory++;
                        money -= Values.memory_price;
                    } else
                    {
                        not_money = true;
                        Res.Sounds.CANT_PLACE.play( );
                    }
                }

                break;

            case PLACE_SHELF:

                offset_x = warehouse_default_x + spectate_offset_x;
                offset_y = warehouse_default_y + spectate_offset_y;

                spectate_warehouse( delta );

                if ( back_from_shelf_placing.pressed( ) )
                {
                    change_mode( MODE.SHOP );
                    return;
                }

                if ( Gdx.input.isKeyJustPressed( Input.Keys.R ) )
                {
                    shelf_vertical = !shelf_vertical;
                }

                if ( Gdx.input.isButtonPressed( Input.Buttons.LEFT ) )
                {
                    //placement
                    if ( possesed_shelves - list_shelves.size > 0 )
                    {
                        if ( warehouse.place_shelf( click_tilex, click_tiley ) )
                        {

                        } else
                        {
                            if ( warehouse.bound_check( click_tilex, click_tiley ) )
                            {

                            }
                        }
                    }
                } else if ( Gdx.input.isButtonPressed( Input.Buttons.RIGHT ) )
                {
                    //take shelf
                    for ( int i = list_shelves.size - 1; i >= 0; i-- )
                    {
                        Shelf s = list_shelves.get( i );
                        if ( s.contains( click_tilex, click_tiley ) )
                        {
                            if ( list_shelves.size < 2 )
                            {
                            } else
                            {
                                if ( warehouse.remove_shelf( s ) )
                                {
                                    list_shelves.removeIndex( i );
                                }
                            }
                        }
                    }
                }
                //back
                break;
            case EXPAND_WAREHOUSE:
                if ( button_back_from_expand.pressed( ) )
                {
                    change_mode( MODE.SHOP );
                    return;
                }

                offset_x = warehouse_default_x + spectate_offset_x;
                offset_y = warehouse_default_y + spectate_offset_y;

                spectate_warehouse( delta );

                if ( button_expand.pressed( ) )
                {
                    if ( money >= warehouse.expansion_prize( ) )
                    {
                        money -= warehouse.expansion_prize( );
                        warehouse.expand( );
                    }
                }

                break;
        }
    }

    @Override
    protected void render()
    {
        if ( mode.render_warehouse )
        {
            warehouse.render( offset_x, offset_y );
        }

        if ( mode == MODE.WORKING )
        {
            for ( Entity e : list_entites )
            {
                e.render_main( offset_x, offset_y );
            }
        }
    }

    @Override
    protected void render2()
    {

        //draw ui
        switch ( mode )
        {
            case START:
                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, Main.world_width - 150, 0, 150, Main.world_height );

                Text.draw_wrapped_text( wrapped_start_text, Main.world_width - 140, Main.world_height - 30, false, 9, Values.color_ui_front, 1f );

                if ( show_help )
                {
                    Text.add( "inbox", 41, 56, Values.color_ui_front, true );

                    Text.add( "outbox", 106, 56, Values.color_ui_front, true );
                    Main.draw_box( 27, 63, 26, 8, Values.color_ui_front );
                    Main.draw_box( 93, 63, 26, 8, Values.color_ui_front );

                    Text.add( "shelf", 75, 120, Values.color_ui_front, true );
                    Main.draw_box( 63, 87, ( 3 * 6 ) + 2, ( 5 * 6 + 2 ), Values.color_ui_front );
                } else
                {
                    Text.add( "press space to", 75, 15, Values.color_ui_front, true );
                    Text.add( "center warehouse", 75, 5, Values.color_ui_front, true );
                }

                break;
            case WORKING:

                Main.batch.setColor( Values.color_ui_back );

                //tutorial text
                if ( tutorial_walk )
                {
                    Text.add( "move with [WASD]", 100, 10, Values.color_ui_front, true );
                } else
                {
                    //then show pick up box and stuff
                    if ( tutorial_pick_up )
                    {
                        Text.add( "pick up box with [E]", 100, 10, Values.color_ui_front, true );
                    } else
                    {
                        if ( tutorial_place )
                        {
                            Text.add( "place box with [E]", 100, 10, Values.color_ui_front, true );
                        }
                    }
                }


                Main.batch.draw( Res.pixel, Main.world_width - 100, 0, 100, Main.world_height );

                Text.add( current_day.long_name, Main.world_width - 50, Main.world_height - 10, Values.color_ui_front, true );
                //show day progress bar,
                Main.batch.setColor( Values.color_ui_front2 );
                Main.batch.draw( Res.pixel, 220, 185, 60, 2 );
                Main.batch.setColor( Values.color_ui_front );
                Main.batch.draw( Res.pixel, 220, 185, 60 * timer_day.getPercent( ), 2 );

                Main.batch.setColor( Color.WHITE );


                if ( near_box != null )
                {
                    if ( last_scanned_code != -1 )
                    {
                        if ( last_scanned_code == near_box.code )
                        {
                            Text.add( "box code", Main.world_width - 50, Main.world_height - 30, Values.color_ui_front, true );
                            Text.add( "[" + Text.box_code( near_box.code ) + "]", Main.world_width - 50, Main.world_height - 40, Values.color_ui_front, true );
                        } else
                        {
                            Text.add( "hold [Q] to scan box", Main.world_width - 50, Main.world_height - 30, Values.color_ui_front, true );
                        }
                    } else
                    {
                        Text.add( "hold [Q] to scan box", Main.world_width - 50, Main.world_height - 30, Values.color_ui_front, true );
                    }
                    if ( scanning_progress > 0 )
                    {
                        Text.add( "scanning box", Main.world_width - 50, Main.world_height - 50, Values.color_ui_front, true );
                        Main.batch.setColor( Values.color_ui_front2 );
                        Main.batch.draw( Res.pixel, 220, 145, 60, 2 );
                        Main.batch.setColor( Values.color_ui_front );
                        Main.batch.draw( Res.pixel, 220, 145, 60 * ( scanning_progress / Values.scan_time ), 2 );
                    }
                } else
                {
                    if ( last_scanned_code != -1 )
                    {
                        Text.add( "last scanned code", Main.world_width - 50, Main.world_height - 30, Values.color_ui_front, true );
                        Text.add( "[" + Text.box_code( last_scanned_code ) + "]", Main.world_width - 50, Main.world_height - 40, Values.color_ui_front, true );
                    }
                }

                Text.add( "box requests", Main.world_width - 50, Main.world_height - 70, Values.color_ui_front, true );

                if ( requests.size == 0 )
                {
                    Text.add( "none", Main.world_width - 50, Main.world_height - 90, Values.color_ui_front, true );
                } else
                {
                    Color request_text_color;
                    int pos_x, pos_y;

                    for ( int i = 0; i < requests.size; i++ )
                    {
                        pos_x = 200 + ( ( ( i % 2 ) ) * 50 );
                        pos_y = 110 - ( ( i / 2 ) * 15 );

                        request_text_color = Values.color_ui_front;
                        if ( highlight_reqeusted_box )
                        {
                            if ( requests.get( i ) == highlight_box_id )
                            {
                                request_text_color = Color.GOLD;
                            }
                        }
                        float t = request_penalty.get( requests.get( i ), -100f );

                        Main.batch.setColor( Color.WHITE );
                        Main.batch.draw( Res.ui_request, pos_x, pos_y );

                        if ( t > 0 )
                        {
                            //draw normal bar shrinking to the left
                            float factor = ( t / Values.request_time );
                            if ( factor < 0.15f )
                            {
                                if ( blink )
                                {
                                    Main.batch.setColor( Values.color_ui_red );
                                } else
                                {
                                    Main.batch.setColor( Color.RED );
                                }
                            } else
                            {
                                Main.batch.setColor( Values.color_ui_front );
                            }
                            Main.batch.draw( Res.pixel, pos_x + 25, pos_y + 1, 24 * factor, 2 );
                        } else
                        {
                            //draw red bar growing to the left
                            float factor = ( t / Values.request_time );
                            Main.batch.setColor( Values.color_ui_red );
                            Main.batch.draw( Res.pixel, pos_x + 24, pos_y + 1, 23 * factor, 2 );
                        }

                        Text.add( "[" + Text.box_code( box_map.get( requests.get( i ) ).code ) + "]", pos_x + 25, pos_y + 5, request_text_color, true );
                    }
                }

//                Text.add( "requests: " + requests.size, 2, 190, Color.WHITE );
//                Text.add( "box ids: " + box_ids.size, 2, 180, Color.WHITE );
//                Text.add( "penalties: " + request_penalty.size, 2, 170, Color.WHITE );
//                Text.add( "box map: " + box_map.size, 2, 160, Color.WHITE );
//                Text.add( "claim list size: " + warehouse.claimed_spots( ), 2, 150, Color.WHITE );

                Text.add( "money", Main.world_width - 70, 7, Values.color_ui_front, true );
                Text.add( "" + money, Main.world_width - 30, 7, Values.color_ui_front, true );

                if ( path_x != null && path_y != null )
                {
                    for ( int i = 0; i < path_x.length - 1; i++ )
                    {
                        Main.line( ( int ) ( ( path_x[ i ] * 6 ) - offset_x ), ( int ) ( path_y[ i ] * 6 - offset_y ), ( int ) ( path_x[ i + 1 ] * 6 - offset_x ), ( int ) ( path_y[ i + 1 ] * 6 - offset_y ), Values.color_ui_front );
                    }
                }

                break;
            case WEEKOVER:
                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, Main.world_width - 100, 0, 100, Main.world_height );
                Text.add( "week over", Main.world_width - 50, Main.world_height - 20, Values.color_ui_front, true );

                //// TODO: 12.08.2018 show how much money you made this week

                Text.add( "money", Main.world_width - 70, 7, Values.color_ui_front, true );
                Text.add( "" + money, Main.world_width - 30, 7, Values.color_ui_front, true );

                if ( !show_help )
                {
                    Text.add( "press space to", 100, 15, Values.color_ui_front, true );
                    Text.add( "center warehouse", 100, 5, Values.color_ui_front, true );
                }
                break;
            case SHOP:
                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, 0, 0, Main.world_width, Main.world_height );

                Text.add( "money", 10, 10, Values.color_ui_front, false );
                Text.add( "" + money, 75, 10, Values.color_ui_front, true );

                Text.add( "SHOP", Main.world_width / 2, 180, Values.color_ui_front, true, 1.2f );

                Text.add( "shelves", 80, 110, Values.color_ui_front );
                Text.add( "you have: " + possesed_shelves, 80, 100, Values.color_ui_front );

                Text.add( "room for boxes", 150, 110, Values.color_ui_front );
                Text.add( "price: " + Values.shelf_prive, 150, 100, Values.color_ui_front );

                Text.add( "robot", 80, 80, Values.color_ui_front );
                Text.add( "you have: " + list_robot.size, 80, 70, Values.color_ui_front );

                Main.batch.setColor( Color.WHITE );

                Main.batch.draw( Res.robot[0], 200, 74);

                Text.add( "autonomous", 150, 80, Values.color_ui_front );
                Text.add( "price: " + Values.robot_prize, 150, 70, Values.color_ui_front );

                Main.batch.setColor( Values.color_ui_front2 );
                Main.batch.draw( Res.pixel, 10, 95, 280, 1 );

                if ( not_money )
                {
                    Text.add( "not enough money!", Main.world_width - 55, 10, Values.color_ui_red, true, 1f );
                }

                break;
            case UPGRADE_ROBOTS:
                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, 0, 0, Main.world_width, Main.world_height );

                Text.add( "UPGRADE ROBOTS", Main.world_width / 2, 180, Values.color_ui_front, true, 1.2f );

                Text.add( "moving speed", 80, 80, Values.color_ui_front );
                Text.add( "bonus: +" + robot_speed_extra_speed, 80, 70, Values.color_ui_front );

                Text.add( "robot memory", 80, 110, Values.color_ui_front );
                Text.add( "bonus: " + Values.robot_memory_size + " +" + extra_robot_memory, 80, 100, Values.color_ui_front );

                Text.add( "amount of box codes in memory", 150, 110, Values.color_ui_front );
                Text.add( "price: " + Values.memory_price, 150, 100, Values.color_ui_front );

                //// TODO: 12.08.2018 draw image of robot here?

                Text.add( "price: " + calc_speed_upgrade_cost( ), 150, 70, Values.color_ui_front );

                Text.add( "money", 10, 10, Values.color_ui_front, false );
                Text.add( "" + money, 75, 10, Values.color_ui_front, true );

                Main.batch.setColor( Values.color_ui_front2 );
                Main.batch.draw( Res.pixel, Main.offset_x( 10 ), Main.offset_y( 90 ), 280, 1 );

                if ( not_money )
                {
                    Text.add( "not enough money!", Main.world_width - 55, 10, Values.color_ui_red, true, 1f );
                }

                break;
            case PLACE_SHELF:

                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, Main.world_width - 100, 0, 100, Main.world_height );

                Text.add( "place shelves", Main.world_width - 50, Main.world_height - 20, Values.color_ui_front, true );

                Text.add( "place with", Main.world_width - 50, 140, Values.color_ui_front, true );
                Text.add( "left mouse button", Main.world_width - 50, 130, Values.color_ui_front, true );

                Text.add( "remove with", Main.world_width - 50, 110, Values.color_ui_front, true );
                Text.add( "right mouse button", Main.world_width - 50, 100, Values.color_ui_front, true );

                Text.add( "rotate with [R]", Main.world_width - 50, 160, Values.color_ui_front, true );

                Text.add( "shelves left:", Main.world_width - 60, 7, Values.color_ui_front, true );
                Text.add( "" + ( possesed_shelves - list_shelves.size ), Main.world_width - 20, 7, Values.color_ui_front, false );

                if ( !show_help )
                {
                    Text.add( "press space to", 100, 15, Values.color_ui_front, true );
                    Text.add( "center warehouse", 100, 5, Values.color_ui_front, true );
                }
                break;

            case EXPAND_WAREHOUSE:
                Main.batch.setColor( Values.color_ui_back );
                Main.batch.draw( Res.pixel, Main.world_width - 100, 0, 100, Main.world_height );

                Text.add( "expand warehouse", Main.world_width - 50, Main.world_height - 20, Values.color_ui_front, true );

                Text.add( "expansion cost", Main.world_width - 50, 140, Values.color_ui_front, true );
                Text.add( "" + warehouse.expansion_prize( ), Main.world_width - 50, 130, Values.color_ui_front, true );

                Text.add( "move around", Main.world_width - 50, 110, Values.color_ui_front, true );
                Text.add( "with [WASD]", Main.world_width - 50, 100, Values.color_ui_front, true );


                Text.add( "money", Main.world_width - 70, 7, Values.color_ui_front, true );
                Text.add( "" + money, Main.world_width - 30, 7, Values.color_ui_front, true );

                if ( !show_help )
                {
                    Text.add( "press space to", 100, 15, Values.color_ui_front, true );
                    Text.add( "center warehouse", 100, 5, Values.color_ui_front, true );
                }
                break;
        }


    }

    private void spectate_warehouse( float delta )
    {
        if ( Gdx.input.isKeyPressed( Input.Keys.A ) )
        {
            spectate_offset_x -= delta * Values.camera_speed;
            show_help = false;
        }
        if ( Gdx.input.isKeyPressed( Input.Keys.D ) )
        {
            spectate_offset_x += delta * Values.camera_speed;
            show_help = false;
        }
        if ( Gdx.input.isKeyPressed( Input.Keys.W ) )
        {
            spectate_offset_y += delta * Values.camera_speed;
            show_help = false;
        }
        if ( Gdx.input.isKeyPressed( Input.Keys.S ) )
        {
            spectate_offset_y -= delta * Values.camera_speed;
            show_help = false;
        }

        if ( lerping_to_center )
        {
            spectate_offset_x = MathUtils.lerp( spectate_offset_x, 0, 0.1f );
            spectate_offset_y = MathUtils.lerp( spectate_offset_y, 0, 0.1f );
            if ( Main.distance( spectate_offset_x, spectate_offset_y, 0, 0 ) < 1 )
            {
                lerping_to_center = false;
                show_help = true;
                spectate_offset_x = 0;
                spectate_offset_y = 0;
            }
        } else
        {
            if ( Gdx.input.isKeyPressed( Input.Keys.SPACE ) )
            {
                lerping_to_center = true;

            }
        }
    }

    public int to_tile( float pos )
    {
        if ( pos < 0 ) return -5;
        return ( int ) ( pos / 6f );
    }

    private void quickSort( Array< Entity > list, int low, int high, boolean descending )
    {
        if ( list == null || list.size < 2 )
        {
            return;
        }

        if ( low >= high )
        {
            return;
        }

        // pick the pivot
        int middle = low + ( high - low ) / 2;
        float pivot = list.get( middle ).y( );

        // make left < pivot and right > pivot
        int i = low, j = high;
        while ( i <= j )
        {
            if ( !descending )
            {
                while ( list.get( i ).y( ) < pivot )
                {
                    i++;
                }

                while ( list.get( j ).y( ) > pivot )
                {
                    j--;
                }
            } else
            {
                while ( list.get( i ).y( ) > pivot )
                {
                    i++;
                }

                while ( list.get( j ).y( ) < pivot )
                {
                    j--;
                }
            }

            if ( i <= j )
            {
                list.swap( i, j );
                i++;
                j--;
            }
        }

        // recursively sort two sub parts
        if ( low < j )
        {
            quickSort( list, low, j, descending );
        }

        if ( high > i )
        {
            quickSort( list, i, high, descending );
        }
    }

    public void add_entity( Entity entity )
    {
        if ( entity == null )
        {
            System.out.println( "could not add entity. it is null! " );
            return;
        } else
        {
            list_entites.add( entity );
        }
        if ( entity.type == Entity_Type.BOX )
        {
            list_box.add( ( Box ) entity ); //should always cast!
            box_map.put( entity.id, ( Box ) entity );
        }
        if ( entity.type == Entity_Type.ROBOT )
        {
            list_robot.add( ( Robot ) entity );
        }
    }

    public void new_box_on_shelf( Box box )
    {
        request_timer.put( box.id, new Timer( MathUtils.random( 5, 60f ) ) );
        box_ids.add( box.id );
    }

    public Box find_nearest_box()
    {
        Box nearest = null;
        if ( list_box == null )
        {
            list_box = new Array< Box >( );
            //list is empty so no near box
            nearest = null;
        } else
        {
            int dst, min = Integer.MAX_VALUE;
            for ( Box e : list_box )
            {
                if ( e.status == Box_Status.PLACED )
                {
                    //should be box
                    dst = Main.distance( player.get_x( ), player.get_y( ), e.get_x( ), e.get_y( ) );
                    if ( dst < min && dst < Values.box_pick_up_distance )
                    {
                        min = dst;
                        nearest = e;
                    }
                }
            }
        }
        return nearest;
    }

    public void box_is_out( Box box )
    {
        System.out.println( "box with code " + box.code + " went out" );
        box_ids.removeValue( box.id );
        if ( requests.removeValue( box.id ) )
        {
            System.out.println( "box was requested" );
            float time = request_penalty.get( box.id );
            float factor = time / Values.request_time;
            int reward = ( int ) ( ( ( float ) Values.full_box_delivery_money ) * factor );
            money += reward;
            System.out.println( "reward for box [" + box.code + "] -> " + reward );
            box_map.remove( box.id );
            request_penalty.remove( box.id );
        } else
        {
            System.out.println( "box not requested!" );
        }
    }

    private int calc_speed_upgrade_cost()
    {
        float costf = ( float ) ( 500 * Math.pow( 1.2, robot_speed_extra_speed ) );
        int cost = ( int ) costf;
        return cost;
    }

    private void clear_lists()
    {
        player.holding = null;
        player.reset_position( );

        for ( Robot rb : list_robot )
        {
            rb.reset( );
        }

        request_penalty.clear( );
        requests.clear( );
        box_map.clear( );
        box_ids.clear( );

        for ( int i = list_entites.size - 1; i >= 0; i-- )
        {
            Entity e = list_entites.get( i );
            if ( e.type == Entity_Type.BOX )
            {
                list_entites.removeIndex( i );
            }
        }
        list_box.clear( );
    }

    public void remove_entity( Entity e )
    {
        remove_set.add( e.id );
    }

    private void change_mode( MODE next )
    {
        mode = next;
        update_buttons( );

        switch ( next )
        {
            case START:
                center_camera_on_warehouse_start( );
                break;
            case WEEKOVER:
                center_camera_on_warehouse( );
                break;
            case WORKING:
                //player is defining camera
                break;
            case SHOP:
                if ( list_robot.size == 0 )
                {
                    button_upgrade_robot.hide( );
                }
                break;
            case UPGRADE_ROBOTS:
                break;
            case PLACE_SHELF:
                center_camera_on_warehouse( );
                break;
            case EXPAND_WAREHOUSE:
                center_camera_on_warehouse( );
                break;
        }
    }

    private void update_buttons()
    {
        //hide or show buttons when mode is changing
        for ( MODE m : MODE.values( ) )
        {
            Array< Button > mb = mode_buttons.get( m );
            for ( int i = 0; i < mb.size; i++ )
            {
                Button b = mb.get( i );
                if ( m == mode )
                {
                    b.show( );
                } else
                {
                    b.hide( );
                }
            }
        }
    }

    private void center_camera_on_warehouse_start()
    {
        offset_x = -70 + ( warehouse.width( ) / 2 * 6 );
        offset_y = -100 + ( warehouse.height( ) / 2 * 6 );
        warehouse_default_x = offset_x;
        warehouse_default_y = offset_y;

    }

    private void center_camera_on_warehouse()
    {
        offset_x = -100 + ( warehouse.width( ) / 2 * 6 );
        offset_y = -100 + ( warehouse.height( ) / 2 * 6 );
        warehouse_default_x = offset_x;
        warehouse_default_y = offset_y;

    }

    public void calculate_paths()
    {
        if ( list_path.size > 0 )
        {
            int iterations = 1000;
            while ( true )
            {
                if ( list_path.size > 0 )
                {
                    Path p = list_path.removeIndex( 0 );

                    if ( p.status( ) == Path.Status.SEARCH )
                    {
                        int needed = p.run( );
                        // System.out.println( "[PATH] needed " + needed + " iterations" );
                        if ( needed == 0 ) needed = 1;
                        iterations -= needed;
                    }
                    if ( p.status( ) != Path.Status.SEARCH )
                    {
                        if ( p.status( ) == Path.Status.POST )
                        {
                            p.post( );
                            Path.pool.free( p );
                        } else if ( p.status( ) == Path.Status.FAIL )
                        {
                            Path.pool.free( p );
                        }
                    } else
                    {
                        list_path.add( p );
                    }
                    if ( iterations <= 0 )
                    {
                        // //System.out.println("reached iteration threshold");
                        break;
                    }
                } else
                {
                    break;
                }
            }
        }
        for ( int i = garbage_paths.size - 1; i >= 0; i-- )
        {
            int path_id = garbage_paths.get( i );
            if ( Path.result( path_id ) == Path.Result.DONE || Path.result( path_id ) == Path.Result.FAIL )
            {
                Path.free_result( path_id );
                garbage_paths.removeIndex( i );
            }
        }
    }

    public void garbage_path( int path_id ) //add path here if not used anymore
    {
        if ( path_id >= 0 )
        {
            garbage_paths.add( path_id );
        }
    }

    public int search_path( int start_x, int start_y, int end_x, int end_y )
    {
        int id = Path.next_id( );
        Path p = Path.pool.obtain( );
        p.init( start_x, start_y, end_x, end_y, id, false, warehouse );
        if ( p.status( ) != Path.Status.FAIL )
        {
            list_path.add( p );
        } else
        {
            Path.pool.free( p );
        }
        return id;
    }

    public Box get_box( int box_id )
    {
        return box_map.get( box_id );
    }

    public enum MODE
    {
        WORKING( true ), WEEKOVER( true ), START( true ), SHOP( false ), UPGRADE_ROBOTS( false ), PLACE_SHELF( true ), EXPAND_WAREHOUSE( true );

        public boolean render_warehouse;

        MODE( boolean render_warehouse )
        {
            this.render_warehouse = render_warehouse;
        }
    }


}
