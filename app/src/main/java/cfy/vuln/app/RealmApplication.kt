package cfy.vuln.app

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration


//Realm Initialization
class RealmApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

        var config:RealmConfiguration=RealmConfiguration.Builder().name("mydb3.5.realm").schemaVersion(1).allowWritesOnUiThread(true)
            .allowQueriesOnUiThread(true).build()
        Realm.setDefaultConfiguration(config)
        Realm.getInstance(config)
     }

    override fun onTerminate() {
        Realm.getDefaultInstance().close()
        super.onTerminate()
    }
}