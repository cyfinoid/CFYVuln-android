package cfy.vuln.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Room DB initializa
@Database(entities = [Product::class,OrderedItems::class], version = 10)
abstract class AppDatabase :RoomDatabase(){
    abstract fun productItemDao():ProductItemDao

    companion object{

        @Volatile
        private var INSTANCE:AppDatabase?=null
        fun getDatabase(context: Context):AppDatabase{
            val tempInstance= INSTANCE
            if(tempInstance!=null){
                return tempInstance
            }

            synchronized(this){
                val instance= Room.databaseBuilder(context,AppDatabase::class.java,"ProductDB").fallbackToDestructiveMigration().allowMainThreadQueries().build()
                INSTANCE=instance
                return instance
            }
        }
    }
}