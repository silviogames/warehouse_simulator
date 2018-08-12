package me.silviogames.ld42;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Silvio on 14.04.2016.
 */
public class ScrollingText
{
    public boolean hide = false;
    private String text;
    private float scale, x, old_x, y, width;
    private Color color;
    private int amount_chars, show_chars, offchar, dir = 1, endoff;
    private Timer idle, move_forward, move_backward;
    private boolean moving, centered;


    public ScrollingText( String text, float x, float y, int width, Color color, boolean centered, float scale )
    {
        init( text, x, y, width, color, centered, scale );
        this.old_x = x;
        idle = new Timer( 2 );
        move_forward = new Timer( 0.22f );
        move_backward = new Timer( 0.08f );
    }

    public void init( String text, float x, float y, int width, Color color, boolean centered, float scale )
    {
        this.text = text;
        this.scale = scale;
        this.y = y;
        this.width = width;
        this.centered = centered;
        this.x = x;
        this.old_x = x;
        this.color = color;
        moving = false;
        if ( scale <= 0 )
        {
            this.scale = 1;
        } else
        {
            this.scale = scale;
        }
        this.amount_chars = Text.raw_text( text ).length( );

        for ( int i = 0; i < amount_chars; i++ )
        {
            float size = Text.length( text.substring( i, amount_chars ), scale );
            if ( size <= width )
            {
                endoff = i;
                break;
            }
        }

        if ( centered )
        {
            if ( Text.length( text, scale ) < width )
            {
                this.x = ( int ) ( this.x - ( Text.length( text, scale ) / 2 ) );
            } else
            {
                this.x = ( int ) ( this.x - ( width / 2 ) );
            }
        }
    }

    public void set_color( Color color )
    {
        this.color = color;
    }

    public void change_text( String text )
    {
        init( text, old_x, y, ( int ) width, color, centered, scale );
        reset_off( );
    }

    public void update( float delta )
    {
        if ( !hide )
        {
            if ( moving )
            {
                if ( dir < 0 )
                {
                    if ( move_backward.update( delta ) )
                    {
                        offchar--;
                        if ( offchar < 0 )
                        {
                            offchar = 0;
                            moving = false;
                            dir = 1;
                        }
                    }
                } else
                {
                    if ( move_forward.update( delta ) )
                    {
                        offchar++;
                        if ( offchar > endoff )
                        {
                            offchar--;
                            moving = false;
                            dir = -1;
                        }
                    }
                }
            } else
            {
                if ( idle.update( delta ) )
                {
                    moving = true;
                }
            }
        }
    }

    public void reset_off()
    {
        offchar = 0;
        moving = false;
        idle.reset( );
        dir = 1;
    }

    public void render()
    {
        //debug
//          Main.batch.setColor(Color.GREEN);
//          Main.batch.draw(Res.pixel, x + width, y - 1, 1, 12);
//         Main.batch.setColor(Color.RED);
//         Main.batch.draw(Res.pixel, x, y, 1, 10);
//         Main.batch.draw(Res.pixel, x + (show_chars * 5 * scale), y, 1, 10);
//         Main.batch.setColor(Color.WHITE);

        String todraw = "";
        int i = offchar;
        while ( true )
        {
            String sub = ( text.substring( i, i + 1 ) );
            if ( Text.length( todraw + sub, scale ) <= width )
            {
                todraw += sub;
                i++;
                if ( i >= text.length( ) )
                {
                    break;
                }
            } else
            {
                break;
            }
        }
        Text.add( todraw, x, y, color, scale );
    }


}
