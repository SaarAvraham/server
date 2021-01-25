package saar.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Status {
    @NonNull
    String status;

    @NonNull
    SearchResult2 d;
}
