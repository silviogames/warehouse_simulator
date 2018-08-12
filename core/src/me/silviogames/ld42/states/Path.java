package me.silviogames.ld42.states;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;

import java.util.ArrayDeque;

import me.silviogames.ld42.Main;
import me.silviogames.ld42.warehouse.Warehouse;

/**
 * Created by Silvio on 18.05.2016.
 */
public class Path implements Pool.Poolable
{
    //result arrays
    public static Pool< Path > pool = new Pool< Path >( )
    {
        @Override
        protected Path newObject()
        {
            return new Path( );
        }
    };
    private static int depth, iterations;
    private static Array< Byte > results = new Array< Byte >( );
    private static Array< int[] > x_path = new Array< int[] >( );
    private static Array< int[] > y_path = new Array< int[] >( );
    private static IntArray distance = new IntArray( );
    private static IntArray temp_x = new IntArray( );
    private static IntArray temp_y = new IntArray( );
    //result arrays
    private IntArray nodes_x = new IntArray( );
    private IntArray nodes_y = new IntArray( );
    private IntArray nodes_h = new IntArray( );
    private IntArray nodes_g = new IntArray( );
    private IntArray nodes_p = new IntArray( );

    private IntSet open_nodes = new IntSet( ), closed_nodes = new IntSet( );


    private int start_x, start_y, end_x, end_y, id;
    private Warehouse map;
    private int last_node = -1, check_node = -1;
    private ArrayDeque< Integer > ngbrs = new ArrayDeque< Integer >( );
    private Status status = Status.SEARCH;
    private long total_iterations = 0;
    private boolean only_check, line, same, jumping = false, line_hit = false;
    private Array< int[] > targets = new Array< int[] >( false, 4 );
    private int target_x = -1, target_y = -1;
    private Move move = Move.NONE;
    private int check_x = -1, check_y = -1, vh_step = 0, vh_dir = 0, dia_dx = 0, dia_dy = 0, dia_x = -1, dia_y = -1, jump_x = -1, jump_y = -1;

    public Path()
    {

    }

    public static Result result( int id )
    {
        if ( id < 0 ) return Result.FAIL;
        if ( id < results.size )
        {
            return Result.values( )[ results.get( id ) ];
        } else
        {
            return Result.FAIL;
        }
    }

    public static int distance( int id )
    {
        if ( id < 0 ) return Integer.MAX_VALUE;
        if ( id < distance.size )
        {
            return distance.get( id );
        } else
        {
            return -1;
        }
    }

    public static int[] get_x_path( int id )
    {
        if ( id < x_path.size )
        {
            return x_path.get( id );
        } else
        {
            return null;
        }
    }

    public static int[] get_y_path( int id )
    {
        if ( id < y_path.size )
        {
            return y_path.get( id );
        } else
        {
            return null;
        }
    }

    public static int next_id()
    {
        int index = 0;
        while ( true )
        {
            if ( index < results.size )
            {
                if ( Result.values( )[ results.get( index ) ] == Result.FREE )
                {
                    results.set( index, Result.SEARCH.ord( ) );
                    return index;
                }
            } else
            {
                results.add( Result.SEARCH.ord( ) );
                x_path.add( null );
                y_path.add( null );
                distance.add( -1 );

                return results.size - 1;
            }
            index++;
        }
    }

    public static void free_result( int id )
    {
        if ( id < 0 )
        {
            //  //System.out.println( "path id -1" );
        }
        if ( id < results.size - 1 )
        {
            results.set( id, Result.FREE.ord( ) );
            x_path.set( id, null );
            y_path.set( id, null );
            distance.set( id, -1 );
        } else if ( id == results.size - 1 )
        {
            results.removeIndex( id );
            x_path.removeIndex( id );
            y_path.removeIndex( id );
            distance.removeIndex( id );
        }
    }

    public static int result_size()
    {
        return results.size;
    }

    public static void flush()
    {
        results.clear( );
        x_path.clear( );
        y_path.clear( );
        distance.clear( );
        temp_x.clear( );
        temp_y.clear( );
    }

    public static int manhatten( int x, int y, int x2, int y2 )
    {
        return Math.abs( y - y2 ) + Math.abs( x - x2 );
    }

    public void init( int start_x, int start_y, int end_x, int end_y, int id, boolean only_check, Warehouse map )
    {
        this.map = map;
        this.only_check = only_check;
        this.start_x = start_x;
        this.start_y = start_y;
        this.end_x = end_x;
        this.end_y = end_y;
        this.id = id;

        clear_nodes( );

        if ( end_x < 0 || end_y < 0 )
        {
            //    //System.out.println( "end of path is negative" );
            status = Status.FAIL;
            results.set( id, Result.FAIL.ord( ) );
            return;
        }

        if ( start_x == end_x && start_y == end_y )
        {
            same = true;
            status = Status.POST;
            results.set( id, Result.DONE.ord( ) );
            x_path.set( id, new int[] { start_x } );
            y_path.set( id, new int[] { start_y } );
            distance.set( id, 0 );
            return;
        }

        if ( map.walk( end_x, end_y ) )
        {
            ////System.out.println("[PATH] searching for 1 end");
            //end position is valid
            targets.add( new int[] { end_x, end_y } );
        } else
        {
            //path over adjacent tile is total_iterations
            if ( map.walk( end_x - 1, end_y ) )
            {
                targets.add( new int[] { end_x - 1, end_y } );
            }
            if ( map.walk( end_x + 1, end_y ) )
            {
                targets.add( new int[] { end_x + 1, end_y } );
            }
            if ( map.walk( end_x, end_y - 1 ) )
            {
                targets.add( new int[] { end_x, end_y - 1 } );
            }
            if ( map.walk( end_x, end_y + 1 ) )
            {
                targets.add( new int[] { end_x, end_y + 1 } );
            }

            if ( targets.size == 0 )
            {
                //  //System.out.println( "[PATH] no end is walk" );
                status = Status.FAIL;

                results.set( id, Result.FAIL.ord( ) );
            } else
            {
                // //System.out.println("[PATH] walk ends: " + targets.size);
            }
        }
    }

    private void clear_nodes()
    {
        nodes_x.clear( );
        nodes_y.clear( );
        nodes_p.clear( );
        nodes_g.clear( );
        nodes_h.clear( );
        open_nodes.clear( );
        closed_nodes.clear( );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof Path )
        {
            Path other = ( Path ) obj;
            if ( other.id == this.id && this.start_x == other.start_x && this.start_y == other.start_y && this.end_x == other.end_x && this.end_y == other.end_y )
            {
                return true;
            } else
            {
                return false;
            }
        } else
        {
            return false;
        }
    }

    public int run()
    {
        iterations = 0;
        if ( total_iterations > 500000 )
        {
            status = Status.FAIL;
            results.set( id, Result.FAIL.ord( ) );
            return 0;
        }

        outer:
        while ( true )
        {
            if ( target_x == -1 || target_y == -1 )
            {
                //set a search target
                if ( targets.size == 0 )
                {
                    //         //System.out.println( "[PATH] no more targets to search" );
                    //        //System.out.println( "[PATH] stopped with " + total_iterations + " total iterations" );
                    status = Status.FAIL;
                    results.set( id, Result.FAIL.ord( ) );
                    break;
                } else
                {
                    int[] t = targets.pop( );
                    target_x = t[ 0 ];
                    target_y = t[ 1 ];
                    if ( Main.distance( start_x, start_y, target_x, target_y ) < 100 && direct_line( start_x, start_y, target_x, target_y ) )
                    {
                        ////System.out.println("[PATH] is direct line");
                        status = Status.POST;
                        line = true;
                        break;
                    }
                    jumping = false;
                    clear_nodes( );
                    int start = create_node( start_x, start_y, -1, target_x, target_y );
                    closed_nodes.add( start );
                    ArrayDeque< Integer > start_ngbrs = start_neighbors( start );
                    while ( !start_ngbrs.isEmpty( ) )
                    {
                        Integer ngbr = start_ngbrs.poll( );
                        open_nodes.add( ngbr );
                    }
                }
            } else
            {
                if ( !jumping )
                {
                    if ( ngbrs.isEmpty( ) )
                    {
                        if ( open_nodes.size > 0 )
                        {
                            check_node = best_node( );
                            if ( check_node < 0 )
                            {

                                target_x = -1;
                                target_y = -1;
                                continue;
                            }
                            closed_nodes.add( check_node );
                            if ( nodes_x.get( check_node ) == target_x && nodes_y.get( check_node ) == target_y )
                            {
                                last_node = check_node;
                                status = Status.POST;
                                //    //System.out.println( "[PATH] ended with " + total_iterations + " total iterations" );
                                break;
                            }
                            ngbrs = get_neighbors( check_node );
                        } else
                        {
                            target_x = -1;
                            target_y = -1;
                        }
                    } else
                    {
                        int ngbr = ngbrs.poll( );
                        start_jump( nodes_x.get( ngbr ), nodes_y.get( ngbr ), nodes_x.get( check_node ), nodes_y.get( check_node ) );
                        jumping = true;
                    }
                } else
                {
                    while ( move != Move.DONE )
                    {
                        iterations++;
                        if ( iterations > 500 )
                        {
                            total_iterations += iterations;
                            break outer;
                        }
                        continue_jump( );
                    }

                    if ( jump_x >= 0 && jump_y >= 0 )
                    {
                        int jumpPoint = from_closed( jump_x, jump_y );
                        if ( jumpPoint < 0 )
                        {
                            jumpPoint = create_node( jump_x, jump_y, check_node, target_x, target_y );
                        } else
                        {
                            //no need to process a jumpPoint that has already been dealt with
                            jumping = false;
                            continue;
                        }

                        //distance == distance of parent and from parent to jumpPoint
                        int distance = manhatten( nodes_x.get( jumpPoint ), nodes_y.get( jumpPoint ), check_node ) + nodes_g.get( check_node );

                        //relax IF vertex is not opened (not placed to heap yet) OR shorter distance to it has been found
                        if ( !open_nodes.contains( jumpPoint ) || nodes_g.get( jumpPoint ) > distance )
                        {
                            nodes_g.set( jumpPoint, distance );
                            nodes_p.set( jumpPoint, check_node );
                            //if vertex was not yet opened, open it and place to heap. Else u its position in heap.
                            open_nodes.add( jumpPoint );
                        }
                        jumping = false;
                    } else
                    {
                        jumping = false;
                    }
                }
            }
        }
        return iterations;
    }

    private boolean direct_line( int start_x, int start_y, int end_x, int end_y )
    {
        boolean geht = true;
        int w = end_x - start_x;
        int h = end_y - start_y;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if ( w < 0 )
        {
            dx1 = -1;
            dx2 = -1;
        } else if ( w > 0 )
        {
            dx1 = 1;
            dx2 = 1;
        }
        if ( h < 0 )
        {
            dy1 = -1;
        } else if ( h > 0 ) dy1 = 1;
        int longest = Math.abs( w );
        int shortest = Math.abs( h );
        if ( longest <= shortest )
        {
            longest = Math.abs( h );
            shortest = Math.abs( w );
            if ( h < 0 )
            {
                dy2 = -1;
            } else if ( h > 0 ) dy2 = 1;
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for ( int i = 0; i <= longest; i++ )
        {
            if ( !map.walk( start_x, start_y ) )
            {
                geht = false;
                break;
            }
            numerator += shortest;
            if ( numerator > longest )
            {
                numerator -= longest;
                start_x += dx1;
                start_y += dy1;
            } else
            {
                start_x += dx2;
                start_y += dy2;
            }
        }
        return geht;
    }

    private int create_node( int tilex, int tiley, int parent, int end_x, int end_y )
    {
        int r = nodes_x.size;
        nodes_x.add( tilex );
        nodes_y.add( tiley );
        nodes_p.add( parent );
        //get parent g and add 1
        if ( parent >= 0 )
        {
            int g = nodes_g.get( parent );
            nodes_g.add( g + 1 );
        } else
        {
            nodes_g.add( 1 );
        }
        nodes_h.add( Math.abs( tiley - end_y ) + Math.abs( tilex - end_x ) );

        return r;
    }

    protected ArrayDeque< Integer > start_neighbors( Integer u )
    {
        ArrayDeque< Integer > ngbrs = new ArrayDeque< Integer >( );
        for ( int ix = -1; ix < 2; ix++ )
        {
            for ( int iy = -1; iy < 2; iy++ )
            {
                if ( ix == 0 && iy == 0 )
                {

                } else
                {
                    int x = nodes_x.get( u ) + ix;
                    int y = nodes_y.get( u ) + iy;
                    if ( map.walk( x, y ) )
                    {
                        ngbrs.add( create_node( x, y, u, target_x, target_y ) );
                    }
                }
            }
        }
        return ngbrs;
    }

    private int best_node()
    {
        float f = Float.MAX_VALUE;
        int r = -1;
        IntSet.IntSetIterator isi = open_nodes.iterator( );
        while ( isi.hasNext )
        {
            int o = isi.next( );
            int of = node_f( o );
            if ( of < f )
            {
                r = o;
                f = of;
            }
        }
        if ( r != -1 ) open_nodes.remove( r );
        return r;
    }

    private ArrayDeque< Integer > get_neighbors( Integer u )
    {
        ArrayDeque< Integer > ngbrs = new ArrayDeque< Integer >( );
        Integer parent = nodes_p.get( u );

        if ( parent >= 0 )
        {
            //get direction of movement
            int dy = ( nodes_y.get( u ) - nodes_y.get( parent ) ) / Math.max( Math.abs( nodes_y.get( u ) - nodes_y.get( parent ) ), 1 );
            int dx = ( nodes_x.get( u ) - nodes_x.get( parent ) ) / Math.max( Math.abs( nodes_x.get( u ) - nodes_x.get( parent ) ), 1 );
            int y = nodes_y.get( u );
            int x = nodes_x.get( u );

            //helper booleans, optimization
            boolean validY = false;
            boolean validX = false;

            //CHECK NEIGHBORS

            if ( dx != 0 && dy != 0 )
            { //diagonally

                //natural neighbors
                if ( map.walk( x, y + dy ) )
                {
                    ngbrs.add( create_node( x, y + dy, u, target_x, target_y ) );
                    validY = true;
                }
                if ( map.walk( x + dx, y ) )
                {
                    ngbrs.add( create_node( x + dx, y, u, target_x, target_y ) );
                    validX = true;
                }
                if ( validY || validX )
                {
                    if ( map.walk( x + dx, y + dy ) )
                    { //caused nullpointer without check at one point, no harm in making sure...
                        ngbrs.add( create_node( x + dx, y + dy, u, target_x, target_y ) );
                    }
                }

                //forced neighbors
                if ( !map.walk( x - dx, y ) && validY )
                {
                    ngbrs.add( create_node( x - dx, y + dy, u, target_x, target_y ) );
                }
                if ( !map.walk( x, y - dy ) && validX )
                {
                    ngbrs.add( create_node( x + dx, y - dy, u, target_x, target_y ) );
                }

            } else
            { //vertically
                if ( dx == 0 )
                {
                    if ( map.walk( x, y + dy ) )
                    {
                        //natural neighbor
//                        if (map.walk(y + dy,x)) {
                        ngbrs.add( create_node( x, y + dy, u, target_x, target_y ) );
//                        }
                        //forced neigbors
                        if ( !map.walk( x + 1, y ) )
                        {
                            ngbrs.add( create_node( x + 1, y + dy, u, target_x, target_y ) );
                        }
                        if ( !map.walk( x - 1, y ) )
                        {
                            ngbrs.add( create_node( x - 1, y + dy, u, target_x, target_y ) );
                        }
                    }
                } else
                {//horizontally
                    //natural neighbors
                    if ( map.walk( x + dx, y ) )
                    {
//                        if (map.walk(y,x+dx)) {
                        ngbrs.add( create_node( x + dx, y, u, target_x, target_y ) );
//                        }
                        //forced neighbors
                        if ( !map.walk( x, y + 1 ) )
                        {
                            ngbrs.add( create_node( x + dx, y + 1, u, target_x, target_y ) );
                        }
                        if ( !map.walk( x, y - 1 ) )
                        {
                            ngbrs.add( create_node( x + dx, y - 1, u, target_x, target_y ) );
                        }
                    }
                }

            }
        } else
        {
            //
        }
        return ngbrs;

    }

    private void start_jump( int x, int y, int px, int py )
    {

        int dx = x - px;
        int dy = y - py;

        jump_x = -1;
        jump_y = -1;

        check_x = x;
        check_y = y;

        //diagonal search
        if ( dx != 0 && dy != 0 )
        {
            move = Move.DIAGONAL;
            dia_x = x;
            dia_y = y;
            dia_dx = dx;
            dia_dy = dy;
        } else
        { //vertical search
            if ( dy != 0 )
            {
                move = Move.VERTICAL_JUMP;
                vh_dir = dy;
                vh_step = 0;
            } else
            { //horizontal search
                move = Move.HORIZONTAL_JUMP;
                vh_dir = dx;
                vh_step = 0;
            }
        }
    }

    private void continue_jump()
    {
        switch ( move )
        {
            case DIAGONAL:
                if ( !map.walk( dia_x, dia_y ) )
                {
                    move = Move.DONE;
                    break;
                }

                if ( dia_x == target_x && dia_y == target_y )
                {
                    jump_x = dia_x;
                    jump_y = dia_y;
                    move = Move.DONE;
                    break;
                }

                if ( ( map.walk( dia_x - dia_dx, dia_y + dia_dy ) && !map.walk( dia_x - dia_dx, dia_y ) ) ||
                        ( map.walk( dia_x + dia_dx, dia_y - dia_dy ) && !map.walk( dia_x, dia_y - dia_dy ) ) )
                {

                    jump_x = dia_x;
                    jump_y = dia_y;
                    move = Move.DONE;
                    break;
                }


                move = Move.VERTICAL_CHECK;
                vh_dir = dia_dy;
                vh_step = 0;
                line_hit = false;
                break;
            case VERTICAL_CHECK:
                vertical_check( );
                break;
            case DIAGONAL_AFTER_V:
                if ( line_hit )
                {
                    jump_x = dia_x;
                    jump_y = dia_y;
                    move = Move.DONE;
                } else
                {
                    move = Move.HORIZONTAL_CHECK;
                    vh_dir = dia_dx;
                    vh_step = 0;
                    line_hit = false;
                }
                break;
            case HORIZONTAL_CHECK:
                horizontal_check( );
                break;
            case DIAGONAL_AFTER_H:
                if ( line_hit )
                {

                    jump_x = dia_x;
                    jump_y = dia_y;
                    move = Move.DONE;
                } else
                {

                    //diagonal search recursively
                    if ( map.walk( dia_x + dia_dx, dia_y ) || map.walk( dia_x, dia_y + dia_dy ) )
                    {
                        dia_x += dia_dx;
                        dia_y += dia_dy;
                        move = Move.DIAGONAL;
                    } else
                    {
                        move = Move.DONE;
                    }
                }
                break;
            case VERTICAL_JUMP:
                vertical_jump( );
                break;
            case HORIZONTAL_JUMP:
                horizontal_jump( );
                break;
        }

    }

    private int from_closed( int x, int y )
    {
        IntSet.IntSetIterator osi = closed_nodes.iterator( );
        while ( osi.hasNext )
        {
            int j = osi.next( );
            if ( nodes_x.get( j ) == x && nodes_y.get( j ) == y ) return j;
        }
        return -1;
    }

    private int manhatten( int x, int y, int whereto )
    {
        return Math.abs( y - nodes_y.get( whereto ) ) + Math.abs( x - nodes_x.get( whereto ) );
    }

    private int node_f( Integer node_id )
    {
        return nodes_g.get( node_id ) + nodes_h.get( node_id );
    }

    private void vertical_check()
    {
        if ( !map.walk( dia_x, dia_y + ( vh_step * vh_dir ) ) )
        {

            line_hit = false;
            move = Move.DIAGONAL_AFTER_V;
            return;
        }

        if ( dia_x == target_x && dia_y + ( vh_step * vh_dir ) == target_y )
        {

            line_hit = true;
            move = Move.DIAGONAL_AFTER_V;
            return;
        }

        //jumpnode if has forced neighbor
        if ( ( map.walk( dia_x - 1, dia_y + ( ( vh_step + 1 ) * vh_dir ) ) && !map.walk( dia_x - 1, dia_y + ( vh_step * vh_dir ) ) ) ||
                ( ( map.walk( dia_x + 1, dia_y + ( ( vh_step + 1 ) * vh_dir ) ) && !map.walk( dia_x + 1, dia_y + ( vh_step * vh_dir ) ) ) ) )
        {
            //forced neighbor found

            line_hit = true;
            move = Move.DIAGONAL_AFTER_V;
            return;
        }

        vh_step++;
    }

    private void horizontal_check()
    {
        if ( !map.walk( dia_x + ( vh_step * vh_dir ), dia_y ) )
        {

            line_hit = false;
            move = Move.DIAGONAL_AFTER_H;
            return;
        }

        if ( dia_x + ( vh_step * vh_dir ) == target_x && dia_y == target_y )
        {

            line_hit = true;
            move = Move.DIAGONAL_AFTER_H;
            return;
        }

        //jupmnode if has forced neighbor
        if ( ( map.walk( dia_x + ( ( vh_step + 1 ) * vh_dir ), dia_y + 1 ) && !map.walk( dia_x + ( vh_step * vh_dir ), dia_y + 1 ) ) ||
                ( ( map.walk( dia_x + ( ( vh_step + 1 ) * vh_dir ), dia_y - 1 ) && !map.walk( dia_x + ( vh_step * vh_dir ), dia_y - 1 ) ) ) )
        {
            //forced neighbor found

            line_hit = true;
            move = Move.DIAGONAL_AFTER_H;
            return;
        }

        vh_step++;
    }

    private void vertical_jump()
    {
        if ( !map.walk( check_x, check_y + ( vh_step * vh_dir ) ) )
        {
            //done

            move = Move.DONE;
            return;
        }

        if ( check_x == target_x && check_y + ( vh_step * vh_dir ) == target_y )
        {

            jump_x = check_x;
            jump_y = check_y + ( vh_step * vh_dir );
            move = Move.DONE;
            return;
        }

        //jumpnode if has forced neighbor
        if ( ( map.walk( check_x - 1, check_y + ( ( vh_step + 1 ) * vh_dir ) ) && !map.walk( check_x - 1, check_y + ( vh_step * vh_dir ) ) ) ||
                ( ( map.walk( check_x + 1, check_y + ( ( vh_step + 1 ) * vh_dir ) ) && !map.walk( check_x + 1, check_y + ( vh_step * vh_dir ) ) ) ) )
        {
            //forced neighbor found

            jump_x = check_x;
            jump_y = check_y + ( vh_step * vh_dir );
            move = Move.DONE;
            return;
        }

        vh_step++;

    }

    private void horizontal_jump()
    {
        if ( !map.walk( check_x + ( vh_step * vh_dir ), check_y ) )
        {

            move = Move.DONE;
            return;
        }

        if ( check_x + ( vh_step * vh_dir ) == target_x && check_y == target_y )
        {

            move = Move.DONE;
            jump_x = check_x + ( vh_step * vh_dir );
            jump_y = check_y;
            return;
        }

        //jupmnode if has forced neighbor
        if ( ( map.walk( check_x + ( ( vh_step + 1 ) * vh_dir ), check_y + 1 ) && !map.walk( check_x + ( vh_step * vh_dir ), check_y + 1 ) ) ||
                ( ( map.walk( check_x + ( ( vh_step + 1 ) * vh_dir ), check_y - 1 ) && !map.walk( check_x + ( vh_step * vh_dir ), check_y - 1 ) ) ) )
        {
            //forced neighbor found

            move = Move.DONE;
            jump_x = check_x + ( vh_step * vh_dir );
            jump_y = check_y;
            return;
        }
        vh_step++;

    }

    public void post()
    {
        if ( !same )
        {
            if ( line )
            {
                results.set( id, Result.DONE.ord( ) );
                x_path.set( id, new int[] { target_x } );
                y_path.set( id, new int[] { target_y } );
                distance.set( id, manhatten( start_x, start_y, target_x, target_y ) );
            } else
            {
                if ( only_check )
                {
                    if ( last_node < 0 )
                    {
                        results.set( id, Result.FAIL.ord( ) );
                    } else
                    {
                        results.set( id, Result.DONE.ord( ) );
                        distance.set( id, nodes_g.get( last_node ) );
                        x_path.set( id, new int[] { target_x } );
                        y_path.set( id, new int[] { target_y } );
                    }
                } else
                {
                    temp_x.clear( );
                    temp_y.clear( );

                    if ( last_node < 0 )
                    {
                        results.set( id, Result.FAIL.ord( ) );
                    } else
                    {
                        results.set( id, Result.DONE.ord( ) );
                        distance.set( id, nodes_g.get( last_node ) );
                        while ( last_node >= 0 )
                        {
                            temp_x.add( nodes_x.get( last_node ) );
                            temp_y.add( nodes_y.get( last_node ) );
                            last_node = nodes_p.get( last_node );
                        }

                        int[] x_values = new int[ temp_x.size ];
                        int[] y_values = new int[ temp_y.size ];
                        int array_index = 0;
                        for ( int i = temp_x.size - 1; i >= 0; i-- )
                        {
                            x_values[ array_index ] = temp_x.get( i );
                            y_values[ array_index ] = temp_y.get( i );
                            array_index++;
                        }
                        x_path.set( id, x_values );
                        y_path.set( id, y_values );
                    }
                }
            }
        }
    }

    public Status status()
    {
        return status;
    }

    @Override
    public void reset()
    {
        this.start_x = -1;
        this.start_y = -1;
        this.end_x = -1;
        this.end_y = -1;
        this.target_x = -1;
        this.target_y = -1;
        this.last_node = -1;
        this.id = -1;
        this.check_node = -1;
        this.ngbrs.clear( );
        this.line = false;
        this.same = false;
        this.status = Status.SEARCH;
        this.targets.clear( );
        this.move = Move.NONE;
        this.line_hit = false;
        this.jumping = false;
        this.check_x = -1;
        this.check_y = -1;
        this.vh_step = 0;
        this.vh_dir = 0;
        this.dia_dx = 0;
        this.dia_dy = 0;
        this.dia_x = -1;
        this.dia_y = -1;
        this.jump_x = -1;
        this.jump_y = -1;
        this.total_iterations = 0;

    }

    public enum Status
    {
        SEARCH, DONE, FAIL, POST
    }

    public enum Result
    {
        FREE, SEARCH, DONE, FAIL;

        public byte ord()
        {
            return ( byte ) ordinal( );
        }

    }

    private enum Move
    {
        NONE, DIAGONAL, DIAGONAL_AFTER_V, DIAGONAL_AFTER_H, VERTICAL_JUMP, HORIZONTAL_JUMP, VERTICAL_CHECK, HORIZONTAL_CHECK, DONE
    }
}
