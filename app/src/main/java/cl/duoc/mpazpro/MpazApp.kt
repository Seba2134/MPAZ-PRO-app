package cl.duoc.mpazpro

import android.app.Application
import cl.duoc.mpazpro.data.AppContainer

/** La clase de la aplicación: vive mientras la app esté abierta y guarda el contenedor. */
class MpazApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}