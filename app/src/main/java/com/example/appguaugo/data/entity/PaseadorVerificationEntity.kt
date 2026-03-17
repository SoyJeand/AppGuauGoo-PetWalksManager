package com.example.appguaugo.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// Los estados posibles de la verificación
object VerificationStatus {
    const val NO_VERIFICADO = "No verificado"
    const val PENDIENTE = "Pendiente"
    const val VERIFICADO = "Verificado"
    const val RECHAZADO = "Rechazado"
}

@Entity(
    tableName = "paseador_verificacion",
    foreignKeys = [
        ForeignKey(
            entity = ClienteEntity::class,
            parentColumns = ["id_cliente"],
            childColumns = ["paseadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaseadorVerificacionEntity(
    @PrimaryKey
    val paseadorId: Int, // Mismo ID que el cliente

    val fotoDocumentoFrenteUri: String?,
    val fotoDocumentoReversoUri: String?,
    val fotoSelfieConDocumentoUri: String?,

    val estado: String = VerificationStatus.NO_VERIFICADO
)

