package lk.ijse.SE10_NETWORK_BACKEND.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private Long studentId;
    private String name;
}
