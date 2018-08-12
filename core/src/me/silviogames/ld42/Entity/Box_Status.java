package me.silviogames.ld42.Entity;

public enum Box_Status
{
    IN( "i" ), OUT( "o" ), PLACED( "p" ), CARRIED( "c" );

    public final String short_name;

    Box_Status( String short_name )
    {
        this.short_name = short_name;
    }

}
