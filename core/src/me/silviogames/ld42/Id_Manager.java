package me.silviogames.ld42;

import com.badlogic.gdx.utils.ObjectIntMap;

public class Id_Manager
{

    private ObjectIntMap< ID_Type > last_id = new ObjectIntMap< ID_Type >( );

    public Id_Manager()
    {
        for ( ID_Type type : ID_Type.values( ) )
        {
            last_id.put( type, 0 );
        }
    }

    public int new_ID(ID_Type type){
        if(type == null){
            System.out.println("[ID MANAGER] get new id, type is null" );
            return -1;
        }else{
            return last_id.getAndIncrement( type, 0, 1 );
        }
    }

    public int get_current_id( ID_Type type )
    {
        if ( type == null )
        {
            return -1;
        } else
        {
            return last_id.get( type, -1 );
        }
    }

}
