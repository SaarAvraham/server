package saar.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchResult {
    long segmentId;
    Instant contactStartTime;
}
