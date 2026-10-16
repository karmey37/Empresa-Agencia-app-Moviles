package cl.duoc.agenciaoportunidades.data

import cl.duoc.agenciaoportunidades.model.AreaContacto
import cl.duoc.agenciaoportunidades.model.Comuna
import cl.duoc.agenciaoportunidades.model.Contacto
import cl.duoc.agenciaoportunidades.model.EstadoOportunidad
import cl.duoc.agenciaoportunidades.model.Evento
import cl.duoc.agenciaoportunidades.model.HistorialEstado
import cl.duoc.agenciaoportunidades.model.Oportunidad
import cl.duoc.agenciaoportunidades.model.Organizacion
import cl.duoc.agenciaoportunidades.model.PerfilUsuario
import cl.duoc.agenciaoportunidades.model.Prioridad
import cl.duoc.agenciaoportunidades.model.ResultadoActivacion
import cl.duoc.agenciaoportunidades.model.Seguimiento
import cl.duoc.agenciaoportunidades.model.TipoAccion
import cl.duoc.agenciaoportunidades.model.TipoEvento
import cl.duoc.agenciaoportunidades.model.TipoOrganizacion
import cl.duoc.agenciaoportunidades.model.Usuario

private const val DIA = 24L * 60 * 60 * 1000

// Fecha relativa a hoy: enDias(5) = dentro de 5 días, enDias(-3) = hace 3 días
private fun enDias(dias: Int): Long = System.currentTimeMillis() + dias * DIA

// Carga datos ficticios solo si la base de datos está vacía
suspend fun cargarDatosIniciales(db: AppDatabase) {
    if (db.usuarioDao().contar() > 0) return

    // ---------- Helpers ----------
    suspend fun comuna(nombre: String, region: String): Int =
        db.comunaDao().insertar(Comuna(nombre = nombre, region = region)).toInt()

    suspend fun usuario(nombre: String, correo: String, password: String, perfil: PerfilUsuario, zona: String): Int =
        db.usuarioDao().insertar(
            Usuario(nombre = nombre, correo = correo, password = password, perfil = perfil, zona = zona)
        ).toInt()

    suspend fun organizacion(nombre: String, tipo: TipoOrganizacion, rubro: String, comunaId: Int, publico: String): Int =
        db.organizacionDao().insertar(
            Organizacion(
                nombre = nombre,
                tipo = tipo,
                rubro = rubro,
                comunaId = comunaId,
                sitioWeb = "https://www.ejemplo-demo.cl",
                publicoAsociado = publico
            )
        ).toInt()

    suspend fun contacto(orgId: Int, nombre: String, cargo: String, area: AreaContacto, correo: String, telefono: String): Int =
        db.contactoDao().insertar(
            Contacto(
                organizacionId = orgId,
                nombre = nombre,
                cargo = cargo,
                area = area,
                correo = correo,
                telefono = telefono,
                fechaUltimoContacto = enDias(-5)
            )
        ).toInt()

    suspend fun evento(
        orgId: Int, comunaId: Int, nombre: String, tipo: TipoEvento,
        dias: Int, horario: String, lugar: String, asistentes: Int, costo: Int
    ): Int {
        val inicio = enDias(dias)
        return db.eventoDao().insertar(
            Evento(
                organizacionId = orgId,
                comunaId = comunaId,
                nombre = nombre,
                tipo = tipo,
                descripcion = "Actividad ficticia de prueba: $nombre.",
                fechaInicio = inicio,
                fechaTermino = inicio + DIA,
                horario = horario,
                ubicacion = lugar,
                publicoEsperado = "Trabajadores y sus familias",
                publicoObjetivo = "Colaboradores de la organización",
                asistentesEstimados = asistentes,
                condiciones = "Stand de 3x3 m, a confirmar con la organización",
                requisitos = "Presentación previa de propuesta",
                costoReferencial = costo,
                espaciosDisponibles = 5
            )
        ).toInt()
    }

    suspend fun oportunidad(
        eventoId: Int, responsableId: Int?, nombre: String,
        prioridad: Prioridad, potencial: Prioridad, afinidad: Prioridad,
        estado: EstadoOportunidad, diasDeteccion: Int
    ): Int {
        val id = db.oportunidadDao().insertar(
            Oportunidad(
                eventoId = eventoId,
                responsableId = responsableId,
                nombre = nombre,
                descripcion = "Oportunidad ficticia: $nombre.",
                fuente = "Redes sociales",
                fechaDeteccion = enDias(diasDeteccion),
                prioridad = prioridad,
                potencialComercial = potencial,
                afinidadPublico = afinidad,
                estado = estado,
                fechaActualizacion = enDias(diasDeteccion)
            )
        ).toInt()
        db.historialEstadoDao().insertar(
            HistorialEstado(
                oportunidadId = id,
                estadoAnterior = null,
                estadoNuevo = estado,
                usuarioId = responsableId,
                fecha = enDias(diasDeteccion),
                observaciones = "Registro inicial"
            )
        )
        return id
    }

    suspend fun seguimiento(
        oportunidadId: Int, usuarioId: Int, contactoId: Int?, accion: TipoAccion,
        dias: Int, resultado: String, proxima: String, diasProximo: Int?
    ) {
        db.seguimientoDao().insertar(
            Seguimiento(
                oportunidadId = oportunidadId,
                usuarioId = usuarioId,
                contactoId = contactoId,
                tipoAccion = accion,
                fechaHora = enDias(dias),
                resultado = resultado,
                proximaAccion = proxima,
                fechaProximoSeguimiento = diasProximo?.let { enDias(it) }
            )
        )
    }

    // ---------- Comunas ----------
    val puenteAlto = comuna("Puente Alto", "Metropolitana de Santiago")
    val santiago = comuna("Santiago", "Metropolitana de Santiago")
    val maipu = comuna("Maipú", "Metropolitana de Santiago")
    val vina = comuna("Viña del Mar", "Valparaíso")
    val concepcion = comuna("Concepción", "Biobío")
    val laSerena = comuna("La Serena", "Coquimbo")
    comuna("Valparaíso", "Valparaíso")
    comuna("Rancagua", "O'Higgins")
    comuna("Temuco", "La Araucanía")

    // ---------- Usuarios (3 ejecutivos + 1 supervisor) ----------
    val ejecutivo1 = usuario("Camila Rojas", "ejecutivo1@agencia.cl", "Ejec1234", PerfilUsuario.EJECUTIVO, "Zona Centro")
    val ejecutivo2 = usuario("Diego Fuentes", "ejecutivo2@agencia.cl", "Ejec1234", PerfilUsuario.EJECUTIVO, "Zona Norte")
    val ejecutivo3 = usuario("Valentina Mora", "ejecutivo3@agencia.cl", "Ejec1234", PerfilUsuario.EJECUTIVO, "Zona Sur")
    usuario("Andrés Paredes", "supervisor@agencia.cl", "Super1234", PerfilUsuario.SUPERVISOR, "Nacional")

    // ---------- Organizaciones ----------
    val constructora = organizacion("Constructora Andes Demo", TipoOrganizacion.EMPRESA, "Construcción", santiago, "Trabajadores y familias")
    val municipalidad = organizacion("Municipalidad Sur Demo", TipoOrganizacion.MUNICIPALIDAD, "Gobierno local", puenteAlto, "Vecinos y funcionarios")
    val centroComercial = organizacion("Centro Comercial Plaza Demo", TipoOrganizacion.CENTRO_COMERCIAL, "Retail", maipu, "Público general")
    val asociacion = organizacion("Asociación Gremial Costa Demo", TipoOrganizacion.ASOCIACION, "Gremial", vina, "Socios y familias")
    val corporacion = organizacion("Corporación Educativa Biobío Demo", TipoOrganizacion.INSTITUCION_PRIVADA, "Educación", concepcion, "Docentes y apoderados")
    val productora = organizacion("Productora Norte Demo", TipoOrganizacion.ORGANIZADOR_EVENTOS, "Eventos", laSerena, "Asistentes a ferias")

    // ---------- Contactos ----------
    val contacto1 = contacto(constructora, "Marcela Tapia", "Jefa de Recursos Humanos", AreaContacto.RECURSOS_HUMANOS, "marcela.tapia@demo.cl", "900000001")
    contacto(constructora, "Paula Núñez", "Encargada de Beneficios", AreaContacto.BENEFICIOS, "paula.nunez@demo.cl", "900000002")
    val contacto2 = contacto(municipalidad, "Felipe Araya", "Coordinador de Eventos", AreaContacto.EVENTOS, "felipe.araya@demo.cl", "900000003")
    contacto(municipalidad, "Sergio Olivares", "Administrador", AreaContacto.ADMINISTRACION, "sergio.olivares@demo.cl", "900000004")
    val contacto3 = contacto(centroComercial, "Daniela Vera", "Ejecutiva de Marketing", AreaContacto.MARKETING, "daniela.vera@demo.cl", "900000005")
    contacto(asociacion, "Tomás Lagos", "Encargado de Activaciones", AreaContacto.ACTIVACIONES, "tomas.lagos@demo.cl", "900000006")
    contacto(corporacion, "Ignacia Soto", "Coordinadora de Bienestar", AreaContacto.BIENESTAR, "ignacia.soto@demo.cl", "900000007")
    val contacto6 = contacto(productora, "Matías Cortés", "Productor General", AreaContacto.EVENTOS, "matias.cortes@demo.cl", "900000008")

    // ---------- Eventos ----------
    val evento1 = evento(constructora, santiago, "Feria de Beneficios para Trabajadores", TipoEvento.JORNADA_BENEFICIOS, 5, "10:00 - 16:00", "Casa matriz (ubicación general)", 400, 150000)
    val evento2 = evento(municipalidad, puenteAlto, "Feria Familiar Municipal", TipoEvento.ACTIVIDAD_MUNICIPAL, 12, "11:00 - 18:00", "Plaza principal (ubicación general)", 600, 0)
    val evento3 = evento(centroComercial, maipu, "Expo Comercial de Primavera", TipoEvento.FERIA_EMPRESARIAL, 20, "10:00 - 20:00", "Explanada del centro comercial", 1200, 300000)
    val evento4 = evento(asociacion, vina, "Encuentro Gremial de la Costa", TipoEvento.EVENTO_CORPORATIVO, 35, "09:00 - 14:00", "Salón de eventos (ubicación general)", 250, 80000)
    val evento5 = evento(corporacion, concepcion, "Feria Institucional de Educación", TipoEvento.FERIA_INSTITUCIONAL, 45, "09:00 - 15:00", "Campus principal (ubicación general)", 500, 120000)
    val evento6 = evento(productora, laSerena, "Festival de Verano del Norte", TipoEvento.ACTIVACION_COMERCIAL, 60, "15:00 - 22:00", "Costanera (ubicación general)", 2000, 450000)
    val evento7 = evento(constructora, santiago, "Jornada de Bienestar de Trabajadores", TipoEvento.ACTIVIDAD_TRABAJADORES, 9, "09:00 - 13:00", "Casa matriz (ubicación general)", 300, 100000)
    val evento8 = evento(productora, laSerena, "Feria Empresarial del Norte", TipoEvento.FERIA_EMPRESARIAL, -14, "10:00 - 18:00", "Recinto ferial (ubicación general)", 800, 200000)

    // ---------- Oportunidades (distintos estados, prioridades y responsables) ----------
    val oportunidad1 = oportunidad(evento1, ejecutivo1, "Feria de Beneficios - Constructora Andes", Prioridad.ALTA, Prioridad.ALTA, Prioridad.ALTA, EstadoOportunidad.PROPUESTA_ENVIADA, -20)
    val oportunidad2 = oportunidad(evento2, ejecutivo1, "Feria Familiar - Municipalidad Sur", Prioridad.MEDIA, Prioridad.ALTA, Prioridad.MEDIA, EstadoOportunidad.CONTACTO_REALIZADO, -10)
    val oportunidad3 = oportunidad(evento3, ejecutivo2, "Expo Primavera - Plaza Demo", Prioridad.ALTA, Prioridad.MEDIA, Prioridad.ALTA, EstadoOportunidad.EN_ESPERA_RESPUESTA, -15)
    oportunidad(evento4, null, "Encuentro Gremial - Costa Demo", Prioridad.MEDIA, Prioridad.MEDIA, Prioridad.BAJA, EstadoOportunidad.EVENTO_IDENTIFICADO, -5)
    oportunidad(evento5, null, "Feria Educación - Biobío Demo", Prioridad.MEDIA, Prioridad.ALTA, Prioridad.MEDIA, EstadoOportunidad.EVENTO_IDENTIFICADO, -2)
    val oportunidad6 = oportunidad(evento6, ejecutivo3, "Festival de Verano - Productora Norte", Prioridad.BAJA, Prioridad.MEDIA, Prioridad.MEDIA, EstadoOportunidad.OPORTUNIDAD_DESCARTADA, -40)
    val oportunidad7 = oportunidad(evento7, ejecutivo3, "Jornada de Bienestar - Constructora Andes", Prioridad.ALTA, Prioridad.ALTA, Prioridad.ALTA, EstadoOportunidad.PARTICIPACION_CONFIRMADA, -30)
    val oportunidad8 = oportunidad(evento8, ejecutivo2, "Feria Empresarial Norte (realizada)", Prioridad.MEDIA, Prioridad.MEDIA, Prioridad.MEDIA, EstadoOportunidad.ACTIVACION_REALIZADA, -60)

    // ---------- Seguimientos ----------
    // Oportunidad 1: último seguimiento ya vencido -> gestión atrasada
    seguimiento(oportunidad1, ejecutivo1, contacto1, TipoAccion.PRIMER_CONTACTO, -14, "Se presentó la agencia por correo", "Enviar propuesta", -8)
    seguimiento(oportunidad1, ejecutivo1, contacto1, TipoAccion.ENVIO_PROPUESTA, -6, "Propuesta enviada, sin respuesta aún", "Llamar para confirmar recepción", -1)
    // Oportunidades 2, 3 y 7: seguimientos pendientes a futuro
    seguimiento(oportunidad2, ejecutivo1, contacto2, TipoAccion.PRIMER_CONTACTO, -3, "Coordinador interesado en participar", "Enviar presentación", 2)
    seguimiento(oportunidad3, ejecutivo2, contacto3, TipoAccion.ENVIO_PRESENTACION, -9, "Presentación enviada", "Esperar respuesta", -3)
    seguimiento(oportunidad3, ejecutivo2, contacto3, TipoAccion.LLAMADA, -2, "Sin respuesta, se dejó mensaje", "Volver a llamar", 4)
    seguimiento(oportunidad7, ejecutivo3, contacto1, TipoAccion.CONFIRMACION, -4, "Participación confirmada por la empresa", "Coordinar montaje del stand", 6)
    // Oportunidades cerradas: sin próxima acción
    seguimiento(oportunidad6, ejecutivo3, contacto6, TipoAccion.CIERRE, -20, "Descartada por baja afinidad con el público", "", null)
    seguimiento(oportunidad8, ejecutivo2, contacto6, TipoAccion.CIERRE, -13, "Activación realizada con buenos resultados", "", null)

    // ---------- Resultado de la activación realizada ----------
    db.resultadoActivacionDao().insertar(
        ResultadoActivacion(
            oportunidadId = oportunidad8,
            fechaRealizacion = enDias(-14),
            asistentesEstimados = 800,
            contactosGenerados = 120,
            personasInteresadas = 65,
            inscripciones = 28,
            evaluacionGeneral = 4,
            aprendizajes = "Llegar con material impreso y más personal en horas punta"
        )
    )
}