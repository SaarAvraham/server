package saar.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude
@ToString
@EqualsAndHashCode
public class CallIdentifier {
    public int sourceId;
    public long segmentId;
}
