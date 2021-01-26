package saar.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude
@ToString
@EqualsAndHashCode
public class Request {
    public String version;
    public Instant eventTimeUTC;
    public PolicyIdentifier policyIdentifier;
    public CallIdentifier callIdentifier;
    @JsonProperty("MediaTypesToDelete")
    public List<String> mediaTypesToDelete;
}
