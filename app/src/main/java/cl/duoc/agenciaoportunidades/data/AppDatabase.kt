package cl.duoc.agenciaoportunidades.data

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.model.Evidencia
import cl.duoc.agenciaoportunidades.model.HistorialEstado
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.Organizacion
import cl.duoc.agenciaoportunidades.model.ResultadoActivacion
import cl.duoc.agenciaoportunidades.model.Seguimiento
import cl.duoc.agenciaoportunidades.model.Usuario

@Database(
    entities = [
        Usuario::class,
        Comuna::class,
        Organizacion::class,
        Contacto::class,
        Evento::class,
        Oportunidad::class,
        Seguimiento::class,
        HistorialEstado::class,
        ResultadoActivacion::class,
        Evidencia::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun comunaDao(): ComunaDao
    abstract fun organizacionDao(): OrganizacionDao
    abstract fun contactoDao(): ContactoDao
    abstract fun eventoDao(): EventoDao
    abstract fun oportunidadDao(): OportunidadDao
    abstract fun seguimientoDao(): SeguimientoDao
    abstract fun historialEstadoDao(): HistorialEstadoDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun resultadoActivacionDao(): ResultadoActivacionDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agencia.db"
                )
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}