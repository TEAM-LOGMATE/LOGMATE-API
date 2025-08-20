package com.logmate.agentConfig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParserDetailConfig {
    private String timestampPattern;
    private String timezone;
}
