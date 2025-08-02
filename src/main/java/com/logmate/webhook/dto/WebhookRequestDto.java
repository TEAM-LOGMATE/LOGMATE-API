package com.logmate.webhook.dto;

import com.logmate.webhook.model.WebhookType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebhookRequestDto {
    private WebhookType type;
    private String url;
}
