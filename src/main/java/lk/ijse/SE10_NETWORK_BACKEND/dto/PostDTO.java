package lk.ijse.SE10_NETWORK_BACKEND.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostDTO implements Serializable {
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private Long approvedBy;

    public PostDTO(String content, Long userId) {
        this.content = content;
        this.userId = userId;
    }
}

