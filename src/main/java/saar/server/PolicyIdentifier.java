package saar.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude
@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
//@MappedSuperclass
public class PolicyIdentifier implements Serializable {
    private int policyId;
    private int policyInstanceId;

    @JsonCreator
    public PolicyIdentifier(@JsonProperty("policyId") Integer policyId, @JsonProperty("policyInstanceId") Integer policyInstanceId){
        if (policyId == null) {
            throw new NullPointerException();
        }

        if (policyInstanceId == null) {
            throw new NullPointerException();
        }

        this.policyId = policyId;
        this.policyInstanceId = policyInstanceId;
    }
}
