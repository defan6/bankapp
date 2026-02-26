package com.bankapp.userservice.domain.detail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ProblemDetail;

import java.time.Instant;

@Getter
@Setter
public class CustomProblemDetail extends ProblemDetail {

    private Instant timestamp;
}
