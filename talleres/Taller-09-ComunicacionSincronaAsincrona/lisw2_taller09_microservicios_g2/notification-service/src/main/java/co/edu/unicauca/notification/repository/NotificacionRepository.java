package co.edu.unicauca.notification.repository;

import co.edu.unicauca.notification.model.Notificacion;
import co.edu.unicauca.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    boolean existsByEventIdAndDestinatarioId(String eventId, Long destinatarioId);

    @Query("select n from Notificacion n where (:destinatario is null or n.destinatarioId = :destinatario) "
            + "and (:tipo is null or n.tipo = :tipo) order by n.id desc")
    List<Notificacion> buscar(@Param("destinatario") Long destinatario, @Param("tipo") NotificationType tipo);
}
