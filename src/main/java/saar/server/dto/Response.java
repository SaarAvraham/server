package saar.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude
@ToString
@EqualsAndHashCode
public class Response {
    public String version;
    public Instant eventTimeUTC;
    public String action;
    public String reason;
    public PolicyIdentifier policyIdentifier;
    public ComplianceCallIdentifier complianceCallIdentifier;
    public StatusByMedia statusByMedia;
    public List<Task> tasks;


    public class PolicyIdentifier{
        public int policyId;
        public int policyInstanceId;
    }

    public class ComplianceCallIdentifier{
        public long segmentId;
    }

    public class StatusByMedia{
        @JsonProperty("Screen")
        public String screen;
        @JsonProperty("Voice")
        public String voice;
    }

    public class Task{
        public String mediaType;
        public String storageType;
        public String status;
        public String reason;
        public Date executionTimeUTC;
        public long sessionId;
    }

}
