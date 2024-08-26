package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InspireDTO implements Serializable {
    private Long inspireId;
    private Long postId;
    private Long userId;

    public InspireDTO(Long postId, Long userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
