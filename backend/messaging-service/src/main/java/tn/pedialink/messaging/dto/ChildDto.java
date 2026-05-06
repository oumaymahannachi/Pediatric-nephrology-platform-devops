package tn.pedialink.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChildDto {
    private String id;
    private String firstName;
    private String lastName;
    private String parentId;
}
