package me.silviogames.ld42.warehouse;

public enum Days
{
    MONDAY( "mo", "monday" ),
    TUESDAY( "tu", "tuesday" ),
    WEDNESDAY( "we", "wednesday" ),
    THURSDAY( "th", "thursday" ),
    FRIDAY( "fr", "friday" ),
    SATURDAY( "sa", "saturday" ),
    SUNDAY( "su", "sunday" );

    public final String short_name, long_name;

    Days( String short_name, String long_name )
    {
        this.short_name = short_name;
        this.long_name = long_name;
    }

    public Days next_day()
    {
        if ( this == SUNDAY )
        {
            return MONDAY;
        } else
        {
            int next_ord = this.ordinal( ) + 1;
            return values( )[ next_ord ];
            //should not return ull since sunday is checked
        }

    }

}
