package me.silviogames.ld42.warehouse;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongMap;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.Res;
import me.silviogames.ld42.Values;
import me.silviogames.ld42.states.State_game;

public class Warehouse
{

    final State_game game;
    int[][] box_map; //save ids of placed boxes here
    Tile_Type[][] type_map; //save type of tile here -> shelf etc.
    IntArray box_codes = new IntArray( ), used_codes = new IntArray( );
    IntArray tilex_inbox = new IntArray( ), tiley_inbox = new IntArray( ); // list of inbox tiles //has to be updated if warehouse size increases
    IntArray tilex_shelf = new IntArray( ), tiley_shelf = new IntArray( ); // list of shelf tiles //has to be updated when new shelfs are places
    IntArray tilex_outbox = new IntArray( ), tiley_outbox = new IntArray( ); // list of outbox tiles //has to be updated when warehouse changes

    LongMap< Integer > claimed_place_tiles = new LongMap< Integer >( ); //long is combination of x and y, integer is id of claiming entity

    private int width, height; //tile based world 6x6 tiles

    public Warehouse( State_game game )
    {
        //place the shelves somehow

        this.game = game;
        this.width = Values.warehouse_starting_width;
        this.height = Values.warehouse_starting_height;

        box_map = new int[ width ][ height ];
        type_map = new Tile_Type[ width ][ height ];

        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                type_map[ i ][ j ] = Tile_Type.FLOOR;
                box_map[ i ][ j ] = -1;
            }
        }

        for ( int i = 0; i < 10000; i++ )
        {
            box_codes.add( i );
        }

        //add in and outbox
        int tx, ty;
        for ( int i = 0; i < Values.in_out_box_length; i++ )
        {
            tx = i;
            ty = 0;
            if ( bound_check( tx, ty ) )
            {
                type_map[ tx ][ ty ] = Tile_Type.IN_BOX;
                tilex_inbox.add( tx );
                tiley_inbox.add( ty );
            }

            tx = width - i - 1;
            ty = 0;

            if ( bound_check( tx, ty ) )
            {
                type_map[ tx ][ ty ] = Tile_Type.OUT_BOX;
                tilex_outbox.add( tx );
                tiley_outbox.add( ty );
            }
        }

        place_shelf( 6, 4 );


    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    public void change_tile( int tilex, int tiley, Tile_Type type )
    {
        if ( !bound_check( tilex, tiley ) ) return;
        if ( type == null ) return;
        type_map[ tilex ][ tiley ] = type;
    }


    public void render( float offset_x, float offset_y )
    {
        if ( game.mode == State_game.MODE.EXPAND_WAREHOUSE )
        {
            //draw bigger warehouse sillouttedw

            Main.batch.setColor( Values.color_expand_silu );
            Main.batch.draw( Res.pixel, -6 - offset_x, 0 - offset_y, ( width + 2 ) * 6, ( height + 1 ) * 6 );
            Main.batch.setColor( Color.WHITE );
        }

        Tile_Type type;
        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                type = type_map[ i ][ j ];
                if ( type != null )
                {
                    if ( type.has_texture )
                    {
                        Main.batch.setColor( Color.WHITE );
                        Main.batch.draw( Res.ground_tiles[ type.ordinal( ) ], i * 6 - offset_x, j * 6 - offset_y );
                    } else
                    {
                        Main.batch.setColor( type.debug_color );
                        Main.batch.draw( Res.pixel, i * 6 - offset_x, j * 6 - offset_y, 6, 6 );
                    }
                }
            }
        }

        if ( bound_check( game.click_tilex, game.click_tiley ) && game.mode == State_game.MODE.PLACE_SHELF )
        {
            if ( shelfpossible( game.click_tilex, game.click_tiley ) && ( game.possesed_shelves - game.list_shelves.size > 0 ) )
            {
                Main.batch.setColor( Values.color_tile_highlight );
            } else
            {
                Main.batch.setColor( Values.color_tile_highlight_invalid );
            }

            //draw shelf preview
            if ( game.shelf_vertical )
            {
                Main.batch.draw( Res.pixel, game.click_tilex * 6 - offset_x, game.click_tiley * 6 - offset_y, 6 * 3, 6 * 5 );
                Main.batch.draw( Res.pixel, ( game.click_tilex + 1 ) * 6 - offset_x, game.click_tiley * 6 - offset_y, 6, 6 * 5 );
                Main.batch.setColor( Color.WHITE );
            } else
            {
                Main.batch.draw( Res.pixel, game.click_tilex * 6 - offset_x, game.click_tiley * 6 - offset_y, 6 * 5, 6 * 3 );
                Main.batch.draw( Res.pixel, ( game.click_tilex ) * 6 - offset_x, ( game.click_tiley + 1 ) * 6 - offset_y, 6 * 5, 6 );
                Main.batch.setColor( Color.WHITE );
            }

        }


        //highlight floor if player is holding box
        if ( game.player.holding != null )
        {
            GridPoint2 place_spot = get_placement_spot( game.player.get_x( ), game.player.y( ) );
            if ( place_spot != null )
            {
                Main.batch.setColor( Values.color_tile_highlight );
                Main.batch.draw( Res.pixel, ( place_spot.x ) * 6 - offset_x, ( place_spot.y ) * 6 - offset_y, 6, 6 );
                Main.batch.setColor( Color.WHITE );
            }
        }

        Main.batch.setColor( Color.WHITE );
    }

    public boolean walk( int tilex, int tiley ) //return true if this position is valid!
    {
        if ( !bound_check( tilex, tiley ) ) return false;

        return type_map[ tilex ][ tiley ].walkable;
    }

    public boolean bound_check( int tilex, int tiley )
    {
        return ( tilex >= 0 && tilex < width && tiley >= 0 && tiley < height );
    }

    public int get_random_tilex()
    {
        return MathUtils.random( 0, width - 1 );
    }

    public int get_random_tiley()
    {
        return MathUtils.random( 0, height - 1 );
    }

    public boolean place_box( int tilex, int tiley, int box_id )
    {
        if ( !bound_check( tilex, tiley ) )
        {
            System.out.println( "cannot place box on invalid tile! out of bounds" );
            return false;
        } else
        {
            if ( type_map[ tilex ][ tiley ].place_boxes )
            {
                if ( box_map[ tilex ][ tiley ] < 0 || box_map[ tilex ][ tiley ] == box_id )
                {
                    //spot not occupied or same as holding
                    box_map[ tilex ][ tiley ] = box_id;
                    return true;
                } else
                {
                    System.out.println( "this spot is already occupied" );
                    return false;
                }
            } else
            {
                System.out.println( "cannot place a box on tile " + type_map[ tilex ][ tiley ].name( ) );
                return false;
            }
        }
    }

    public boolean spawn_box( int tilex, int tiley, int box_id )
    {
        if ( !bound_check( tilex, tiley ) )
        {
            System.out.println( "cannot place box on invalid tile! out of bounds" );
            return false;
        } else
        {
            if ( box_map[ tilex ][ tiley ] < 0 || box_map[ tilex ][ tiley ] == box_id )
            {
                //spot not occupied or same as holding
                box_map[ tilex ][ tiley ] = box_id;
                return true;
            } else
            {
                System.out.println( "this spot is already occupied" );
                return false;
            }
        }
    }

    public boolean pickup_box( int tilex, int tiley, int box_id )
    {
        if ( !bound_check( tilex, tiley ) )
        {
            System.out.println( "cannot pickup box on invalid tile" );
            return false;
        } else
        {
            //dont take from outbox
            if ( type_map[ tilex ][ tiley ] == Tile_Type.OUT_BOX )
            {
                System.out.println( "cannot take from outbox!" );
                return false;
            }

            if ( box_map[ tilex ][ tiley ] == box_id )
            {
                //is same box as expected
                box_map[ tilex ][ tiley ] = -1;
                return true;
            } else
            {
                System.out.println( "tried to pick up box with id " + box_id + " but the box on this tile has id " + box_map[ tilex ][ tiley ] );
                return false;
            }
        }
    }

    public boolean tile_occupied( int tilex, int tiley )
    {
        if ( !bound_check( tilex, tiley ) )
        {
            System.out.println( "checking a invalid tile for box occupation" );
            return true;
        } else
        {
            return box_map[ tilex ][ tiley ] >= 0;
        }
    }

    public GridPoint2 get_free_box_spot()
    {
        if ( free_inbox_slots( ) == 0 ) return null;
        for ( int i = 0; i < tiley_inbox.size; i++ )
        {
            if ( box_map[ tilex_inbox.get( i ) ][ tiley_inbox.get( i ) ] == -1 )
            {
                return new GridPoint2( tilex_inbox.get( i ), tiley_inbox.get( i ) );
            }
        }
        return null;
    }

    public int free_inbox_slots()
    {
        //assuming that inbox arrays are same length
        int count = 0;
        for ( int i = 0; i < tilex_inbox.size; i++ )
        {
            if ( box_map[ tilex_inbox.get( i ) ][ tiley_inbox.get( i ) ] == -1 )
            {
                count++;
            }
        }
        return count;
    }

    public GridPoint3 get_filled_inbox_position()
    {
        int find_tilex = -1, find_tiley = -1;
        for ( int i = 0; i < tilex_inbox.size; i++ )
        {
            if ( box_map[ tilex_inbox.get( i ) ][ tiley_inbox.get( i ) ] != -1 && !claimed_place_tiles.containsKey( Main.xinty( tilex_inbox.get( i ), tiley_inbox.get( i ) ) ) )
            {
                find_tilex = tilex_inbox.get( i );
                find_tiley = tiley_inbox.get( i );
                break;
            }
        }
        if ( find_tilex != -1 && find_tiley != -1 )
        {
            return new GridPoint3( find_tilex, find_tiley, box_map[ find_tilex ][ find_tiley ] );
        } else
        {
            return null;
        }

    }

    public boolean place_shelf( int start_tilex, int start_tiley )
    {
        if ( shelfpossible( start_tilex, start_tiley ) )
        {
            if ( game.shelf_vertical )
            {
                game.list_shelves.add( new Shelf( start_tilex, start_tiley, 3, 5 ) );
                for ( int i = 0; i < 5; i++ )
                {
                    type_map[ start_tilex ][ start_tiley + i ] = Tile_Type.SHELF;
                    tilex_shelf.add( start_tilex );
                    tiley_shelf.add( start_tiley + i );
                    type_map[ start_tilex + 1 ][ start_tiley + i ] = Tile_Type.SHELF_WALL;
                    type_map[ start_tilex + 2 ][ start_tiley + i ] = Tile_Type.SHELF;
                    tilex_shelf.add( start_tilex + 2 );
                    tiley_shelf.add( start_tiley + i );
                }
                return true;
            } else
            {
                game.list_shelves.add( new Shelf( start_tilex, start_tiley, 5, 3 ) );
                for ( int i = 0; i < 5; i++ )
                {
                    type_map[ start_tilex + i ][ start_tiley ] = Tile_Type.SHELF;
                    tilex_shelf.add( start_tilex + i );
                    tiley_shelf.add( start_tiley );
                    type_map[ start_tilex + i ][ start_tiley + 1 ] = Tile_Type.SHELF_WALL;
                    type_map[ start_tilex + i ][ start_tiley + 2 ] = Tile_Type.SHELF;
                    tilex_shelf.add( start_tilex + i );
                    tiley_shelf.add( start_tiley + 2 );
                }
                return true;
            }
        }
        return false;
    }

    public boolean remove_shelf( Shelf shelf )
    {
        if ( shelf == null ) return false;
        for ( int i = 0; i < shelf.width; i++ )
        {
            for ( int j = 0; j < shelf.height; j++ )
            {
                if ( bound_check( shelf.tilex + i, shelf.tiley + j ) )
                {
                    type_map[ shelf.tilex + i ][ shelf.tiley + j ] = Tile_Type.FLOOR;
                    box_map[ shelf.tilex + i ][ shelf.tiley + j ] = -1; //should not be neccecary
                }
            }
        }
        return true;
    }


    private boolean shelfpossible( int start_tilex, int start_tiley )
    {
        //all tiles must be within the room, no box on it and floor tiles to be converted to shelf tile
        int tilex, tiley;
        boolean placement_possible = true;

        if ( start_tiley < 2 ) return false;

        if ( game.shelf_vertical )
        {
            out:
            for ( int i = -1; i < 4; i++ )
            {
                for ( int j = 0; j < 5; j++ )
                {
                    tilex = start_tilex + i;
                    tiley = start_tiley + j;

                    if ( !bound_check( tilex, tiley ) )
                    {
                        placement_possible = false;
                        break out;
                    } else
                    {
                        if ( type_map[ tilex ][ tiley ] != Tile_Type.FLOOR )
                        {
                            placement_possible = false;
                            break out;
                        }
                    }
                }
            }
        } else
        {
            out:
            for ( int i = -1; i < 4; i++ )
            {
                for ( int j = 0; j < 5; j++ )
                {
                    tilex = start_tilex + j;
                    tiley = start_tiley + i;

                    if ( !bound_check( tilex, tiley ) )
                    {
                        placement_possible = false;
                        break out;
                    } else
                    {
                        if ( type_map[ tilex ][ tiley ] != Tile_Type.FLOOR )
                        {
                            placement_possible = false;
                            break out;
                        }
                    }
                }
            }
        }
        return placement_possible;
    }

    public int get_unused_box_code()
    {
        if ( box_codes.size == 0 )
        {
            System.out.println( "no box codes left!" );
            return -9999;
        } else
        {
            int code = box_codes.random( );
            used_codes.add( code );
            return code;
        }
    }

    public void next_week()
    {
        //add used box codes back to codes
        box_codes.addAll( used_codes );
        used_codes.clear( );

        //reset box map
        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                box_map[ i ][ j ] = -1;
            }
        }
    }

    public boolean is_outbox( int tilex, int tiley )
    {
        if ( bound_check( tilex, tiley ) )
        {
            return type_map[ tilex ][ tiley ] == Tile_Type.OUT_BOX;
        } else
        {
            return false;
        }
    }

    public void box_went_out( int tilex, int tiley, int box_id )
    {
        if ( bound_check( tilex, tiley ) )
        {
            if ( box_map[ tilex ][ tiley ] == box_id )
            {
                //as expected
                box_map[ tilex ][ tiley ] = -1;
            } else
            {
                System.out.println( "[WAREHOUSE] box_went_out: did expect id " + box_id + " but found " + box_map[ tilex ][ tiley ] + " on pos " + tilex + " " + tiley );
            }
        } else
        {
            System.out.println( "this box is not in valid map space!" );
        }
    }

    public GridPoint2 get_placement_spot( float player_x, float player_y )
    {
        int player_tilex = ( int ) player_x / 6;
        int player_tiley = ( int ) player_y / 6;

        int check_tilex, check_tiley;
        int near_tilex = -1, near_tiley = -1;

        int dst, min = Integer.MAX_VALUE;

        for ( int i = -3; i < 4; i++ )
        {
            for ( int j = -3; j < 4; j++ )
            {
                check_tilex = player_tilex + i;
                check_tiley = player_tiley + j;
                if ( bound_check( check_tilex, check_tiley ) )
                {
                    //is inside warehouse
                    if ( type_map[ check_tilex ][ check_tiley ] == Tile_Type.SHELF || type_map[ check_tilex ][ check_tiley ] == Tile_Type.OUT_BOX )
                    {
                        //can place box here
                        if ( box_map[ check_tilex ][ check_tiley ] == -1 )
                        {
                            //is empty
                            dst = Main.distance( player_x, player_y, check_tilex * 6 + 3, check_tiley * 6 + 3 );
                            if ( dst < Values.box_place_distance && dst < min )
                            {
                                min = dst;
                                near_tilex = check_tilex;
                                near_tiley = check_tiley;
                            }
                        }
                    }
                }
            }
        }
        if ( near_tilex != -1 )
        {
            return new GridPoint2( near_tilex, near_tiley );
        } else
        {
            return null;
        }
    }

    public GridPoint2 get_free_shelf_spot()
    {
        int find_tilex = -1, find_tiley = -1;
        for ( int i = 0; i < tilex_shelf.size; i++ )
        {
            if ( box_map[ tilex_shelf.get( i ) ][ tiley_shelf.get( i ) ] == -1 && !claimed_place_tiles.containsKey( Main.xinty( tilex_shelf.get( i ), tiley_shelf.get( i ) ) ) )
            {
                find_tilex = tilex_shelf.get( i );
                find_tiley = tiley_shelf.get( i );
                break;
            }
        }
        if ( find_tilex != -1 && find_tiley != -1 )
        {
            return new GridPoint2( find_tilex, find_tiley );
        } else
        {
            return null;
        }
    }

    public int claimed_spots()
    {
        return claimed_place_tiles.size;
    }

    public boolean claim_spot( int tilex, int tiley )
    {
        if ( bound_check( tilex, tiley ) )
        {
            if ( claimed_place_tiles.containsKey( Main.xinty( tilex, tiley ) ) )
            {
                return false;
            } else
            {
                claimed_place_tiles.put( Main.xinty( tilex, tiley ), 0 );
                return true;
            }
        } else
        {
            return false;
        }
    }

    public void unclaim( int tilex, int tiley )
    {
        claimed_place_tiles.remove( Main.xinty( tilex, tiley ) );
    }

    public GridPoint3 get_random_filled_shelfpos()
    {
        IntArray list_found = new IntArray( );
        int find_tilex = -1, find_tiley = -1;
        for ( int i = 0; i < tilex_shelf.size; i++ )
        {
            if ( box_map[ tilex_shelf.get( i ) ][ tiley_shelf.get( i ) ] != -1 )
            {
                list_found.add( i + 1 );
            }
        }
        int found = list_found.random( );
        if ( found != 0 )
        {
            find_tilex = tilex_shelf.get( found - 1 );
            find_tiley = tiley_shelf.get( found - 1 );
        }

        if ( find_tilex != -1 && find_tiley != -1 )
        {
            return new GridPoint3( find_tilex, find_tiley, box_map[ find_tilex ][ find_tiley ] );
        } else
        {
            return null;
        }
    }

    public GridPoint2 get_free_outbox_spot()
    {
        int find_tilex = -1, find_tiley = -1;
        for ( int i = 0; i < tilex_outbox.size; i++ )
        {
            if ( box_map[ tilex_outbox.get( i ) ][ tiley_outbox.get( i ) ] == -1 && !claimed_place_tiles.containsKey( Main.xinty( tilex_outbox.get( i ), tiley_outbox.get( i ) ) ) )
            {
                find_tilex = tilex_outbox.get( i );
                find_tiley = tiley_outbox.get( i );
                break;
            }
        }
        if ( find_tilex != -1 && find_tiley != -1 )
        {
            return new GridPoint2( find_tilex, find_tiley );
        } else
        {
            return null;
        }
    }

    public boolean is_box_here( int tilex, int tiley, int box_id )
    {
        if ( bound_check( tilex, tiley ) )
        {
            return box_map[ tilex ][ tiley ] == box_id;
        } else
        {
            return false;
        }
    }

    public void expand()
    {
        Tile_Type[][] oldmap = type_map;
        width += 2;
        height += 1;
        type_map = new Tile_Type[ width ][ height ];
        box_map = new int[ width ][ height ];

        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                type_map[ i ][ j ] = Tile_Type.FLOOR;
            }
        }

        for ( int i = 0; i < width - 2; i++ )
        {
            for ( int j = 0; j < height - 1; j++ )
            {
                type_map[ i + 1 ][ j ] = oldmap[ i ][ j ];
            }
        }

        tilex_inbox.clear( );
        tiley_inbox.clear( );

        tilex_outbox.clear( );
        tiley_outbox.clear( );

        tilex_shelf.clear( );
        tiley_shelf.clear( );

        for ( int i = 0; i < width; i++ )
        {
            for ( int j = 0; j < height; j++ )
            {
                switch ( type_map[ i ][ j ] )
                {

                    case SHELF:
                        tilex_shelf.add( i );
                        tiley_shelf.add( i );
                        break;
                    case IN_BOX:
                        tilex_inbox.add( i );
                        tiley_inbox.add( i );
                        break;
                    case OUT_BOX:
                        tilex_outbox.add( i );
                        tiley_outbox.add( i );
                        break;
                }
            }
        }
    }

    public int expansion_prize()
    {
        int current_size = width * height;
        int next_size = ( width + 2 ) * ( height ) + 1;

        int diff = next_size - current_size;
        return diff * Values.price_per_warehouse_tile;
    }


}
