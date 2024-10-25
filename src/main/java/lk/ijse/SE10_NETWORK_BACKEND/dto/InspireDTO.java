package lk.ijse.SE10_NETWORK_BACKEND.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InspireDTO implements Serializable {
    private Long inspireId;
    @NotNull(message = "Post ID cannot be null")
    private Long postId;
    private Long userId;
}