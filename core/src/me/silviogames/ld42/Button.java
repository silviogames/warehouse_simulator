/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.silviogames.ld42;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * @author Silvio
 */
public class Button
{

    private static final Color one = new Color( 0.15f, 0.15f, 0.15f, 1 ), two = new Color( 0.6f, 0.6f, 0.6f, 1 );
    private final int width, height;
    private final ButtonType bt;
    private TextureRegion tex;
    private float x, y;
    private float textX = 0, textY = 0, textSize = 1, delay = 0;
    private boolean isPressed, wasoutside, hide = false, on = false, hold;
    private boolean longHold;
    private String text;
    private float holdingTime = 0;
    private Color background = Color.WHITE, background_dimmed = Color.WHITE, text_color = Color.WHITE, single_color = Color.WHITE;
    private Integer id = Main.id_handler.new_ID( ID_Type.BUTTON );
    private Res.Sounds sound = Res.Sounds.WEIRD;

    public Button( int posX, int posY, TextureRegion tex )
    {
        this.x = posX;
        this.y = posY;
        this.tex = tex;
        this.width = tex.getRegionWidth( );
        this.height = tex.getRegionHeight( );
        this.bt = ButtonType.TEXTURE;
    }

    public Button( int posX, int posY, Texture tex )
    {
        this.x = posX;
        this.y = posY;
        this.tex = new TextureRegion( tex, 0, 0, tex.getWidth( ), tex.getHeight( ) );
        this.width = tex.getWidth( );
        this.height = tex.getHeight( );
        this.bt = ButtonType.TEXTURE;
    }

    public Button( int posX, int posY, int width, int height, String text, Color text_color, Color button_color, boolean centered )
    {

        background = button_color;
        background_dimmed = new Color( button_color ).mul( 0.2f );
        this.y = posY;
        //this.width = ( int ) Text.length( text, textSize ) + 3;
        this.height = height;
        this.width = width;

        this.text_color = text_color;
        this.text = text;
        if ( centered )
        {
            this.x = posX - ( width / 2 );
        } else
        {
            this.x = posX;
        }
        this.textX = ( width / 2 ) - ( Text.length( text, textSize ) / 2 );
        this.textY = ( height / 2 ) - 4;
        this.bt = ButtonType.TEXT;
        //this.tex = new TextureRegion( Res.pixel, 0, 0, width - 2, height - 2 );
    }

    public Button( int posX, int posY, String text, float textSize, boolean xCentered )
    {

        this.y = posY;
        this.width = ( int ) Text.length( text, textSize ) + 3;
        this.height = 9;

        this.text = text;
        if ( xCentered )
        {
            this.x = posX - ( width / 2 );
        } else
        {
            this.x = posX;
        }
        this.textX = 2;
        this.textY = 2;
        this.bt = ButtonType.TEXT;
        this.tex = new TextureRegion( Res.button_background, 0, 0, width - 2, height - 2 );
    }

    public Button( int posX, int posY, int width, int height, String text, boolean centered )
    {

        this.y = posY;
        if ( width <= Text.length( text, textSize ) + 3 )
        {
            this.width = ( int ) Text.length( text, textSize ) + 3;
        } else
        {
            this.width = width;
        }
        if ( height <= 9 )
        {
            this.height = 9;
        } else
        {
            this.height = height;
        }

        this.text = text;
        if ( centered )
        {
            this.x = posX - ( width / 2 );
        } else
        {
            this.x = posX;
        }

        this.textX = ( width / 2 ) - ( Text.length( text, textSize ) / 2 );
        this.textY = ( height / 2 ) - 4;
        this.bt = ButtonType.TEXT;
        this.tex = new TextureRegion( Res.button_background, 0, 0, width - 2, height - 2 );

    }

    public Button( int posX, int posY, int width, int height, boolean xCentered )
    {

        this.y = posY;

        this.width = width;

        this.height = height;

        this.bt = ButtonType.SHADOW;
        if ( xCentered )
        {
            this.x = posX - ( width / 2 );
        } else
        {
            this.x = posX;
        }

    }

    public Button( int posX, int posY, int width, int height, Color color )
    {
        this.y = posY;
        this.width = width;
        this.height = height;
        this.bt = ButtonType.SINGLE_COLOR;
        this.single_color = color;
        this.x = posX;
    }

    public Button( int posX, int posY, int width, int height, Color color, boolean xCentered )
    {

        this.y = posY;
        this.single_color = color;
        this.width = width;

        this.height = height;

        this.bt = ButtonType.SINGLE_COLOR;
        if ( xCentered )
        {
            this.x = posX - ( width / 2 );
        } else
        {
            this.x = posX;
        }
    }

    public Button( int posX, int posY, int width, int height )
    {
        this.y = posY;
        this.width = width;
        this.height = height;
        this.bt = ButtonType.SHADOW;
        this.x = posX;
    }

    public Button( int posX, int posY, TextureRegion tex, boolean on )
    {
        this.x = posX;
        this.y = posY;
        this.tex = tex;
        this.width = tex.getRegionWidth( );
        this.height = tex.getRegionHeight( );
        this.bt = ButtonType.SWITCH;
        this.on = on;
    }

    public int get_id()
    {
        return id;
    }

    public boolean update( int pointerX, int pointerY, boolean mousedown, float delta )
    {
        if ( !hide )
        {
            boolean returner = false;

            // reset button if it was pressed the frame before;
            if ( isPressed )
            {
                isPressed = false;
            }

            if ( !mousedown )
            {
                wasoutside = false;
            }

            // if the pointer floats over the button (only on desktop)
            if ( pointerX >= x && pointerX <= x + width && pointerY >= y && pointerY <= y + height )
            {

                if ( !wasoutside )
                {
                    if ( mousedown )
                    {
                        delay += delta;
                        if ( delay >= 0.03f )
                        {
                            hold = true;
                            returner = true;
                            holdingTime += delta;
                            if ( holdingTime >= 1 ) longHold = true;
                        }
                    } else
                    {
                        delay = 0;
                    }

                    if ( hold && !mousedown )
                    {
                        if ( !longHold )
                        {
                            isPressed = true;
                           // sound.play( );
                        }
                        // //System.out.println("pressed button " + (posX / 45));
                        hold = false;
                        returner = false;
                        holdingTime = 0;
                        longHold = false;
                        delay = 0;
                    }
                }
            } else
            {
                if ( hold && !mousedown )
                {
                    hold = false;
                    returner = false;
                    longHold = false;
                    holdingTime = 0;
                }

                if ( mousedown && !hold ) wasoutside = true;

                if ( hold && mousedown )
                {
                    returner = true;
                    longHold = false;
                    holdingTime = 0;
                }
            }

            return returner;
        } else
        {
            delta = 0;
            hold = false;
            longHold = false;
            holdingTime = 0;
            return false;
        }
    }

    public boolean pressed()
    {
        return isPressed;
    }

    public void render( SpriteBatch b )
    {
        if ( !hide )
        {
            switch ( bt )
            {
                case TEXTURE:
                    if ( hold ) b.setColor( Color.GRAY );
                    b.draw( tex, x, y );
                    b.setColor( Color.WHITE );
                    break;
                case TEXT:
                    b.setColor( one );
                    b.draw( Res.pixel, x, y - ( hold ? 1 : 0 ), width, height );
                    if ( hold )
                    {
                        b.setColor( background_dimmed );
                    } else
                    {
                        b.setColor( background );
                    }
                    b.draw( Res.pixel, x + 1, y + ( hold ? 0 : 1 ), width - 1, height - 1 );
                    if ( !hold )
                    {
                        b.setColor( 0f, 0f, 0f, 0.5f );
                        b.draw( Res.pixel, x, y - 1, width, height );
                    }
                    b.setColor( Color.WHITE );
                    Text.add( text, x + textX, y + textY - ( hold ? 1 : 0 ), text_color, textSize );
                    break;
                case SHADOW:
                    if ( hold )
                    {
                        b.setColor( Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.5f );
                        b.draw( Res.pixel, x, y, width, height );
                        b.setColor( Color.WHITE );
                    }
                    break;
                case SINGLE_COLOR:
                    b.setColor( single_color );
                    b.draw( Res.pixel, x, y, width, height );
                    b.setColor( Color.WHITE );
                    if ( hold )
                    {
                        b.setColor( Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 0.5f );
                        b.draw( Res.pixel, x, y, width, height );
                        b.setColor( Color.WHITE );
                    }
                    break;
                default:
                    if ( hold )
                    {
                        b.setColor( Color.DARK_GRAY );
                    } else if ( on )
                    {
                        b.setColor( Color.GRAY );
                    }
                    b.draw( tex, x, y );
                    b.setColor( Color.WHITE );
                    break;
            }
        }
    }

    /**
     * WARNING CREATED BUTTONS WONT RESIZE if the new text is longer than the
     * old one it will overlap but it will rearrange the text in the center
     *
     * @param text that should be changed
     */
    public void changetext( String text )
    {
        this.text = text;
        this.textX = ( width / 2 ) - ( Text.length( text, textSize ) / 2 );
    }

//    public void set_tex_type( TextureType tex_type )
//    {
//        this.texture_type = tex_type;
//        switch ( texture_type )
//        {
//            case WOOD:
//                this.tex = new TextureRegion( Res.button, 0, 0, width - 2, height - 2 );
//                text_color = Color.WHITE;
//                break;
//            case PAPER:
//                this.tex = new TextureRegion( Res.paper, 20, 20, width - 2, height - 2 );
//                text_color = Color.NAVY;
//                break;
//        }
//
//    }

    /**
     * hide the button. it still will be updated but doesnt render and get
     * clicked
     */
    public void hide()
    {
        hide = true;
        isPressed = false;
    }

    public void set_text_size( float size )
    {
        this.textSize = size;
        this.textX = ( width / 2 ) - ( Text.length( text, size ) / 2 );
        this.textY = ( height / 2 ) - ( ( 5 * textSize ) / 2 );

    }

    public void set_sound( Res.Sounds sound )
    {
        if ( sound == null )
        {
            this.sound = Res.Sounds.WEIRD;
        } else
        {
            this.sound = sound;
        }
    }

    public void setBackgroundColor( Color color )
    {
        this.background = color;
    }

    public void set_text_color( Color color )
    {
        this.text_color = color;
    }

    /**
     * show the button, when it was hidden.
     */
    public void show()
    {
        hide = false;
    }

    public void setPosX( int x )
    {
        this.x = x;
    }

    public void setPosY( int y )
    {
        this.y = y;
    }

    public void translate( float x, float y )
    {
        this.x += x;
        this.y += y;
    }

    public void activate()
    {
        on = true;
    }

    public void deactivate()
    {
        on = false;
    }

    public boolean down()
    {
        return hold;
    }

    public boolean long_down()
    {
        return longHold;
    }

    private enum ButtonType
    {
        SHADOW, TEXT, TEXTURE, SWITCH, SINGLE_COLOR;
    }


}
