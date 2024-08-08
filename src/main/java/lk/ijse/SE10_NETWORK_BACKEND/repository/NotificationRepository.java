package lk.ijse.SE10_NETWORK_BACKEND.repository;

import lk.ijse.SE10_NETWORK_BACKEND.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
