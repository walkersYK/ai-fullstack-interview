package com.lcysgsg.interview.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceDisableRequest(
        @NotBlank(message = "停用原因不能为空")
        @Size(max = 200, message = "停用原因不能超过 200 个字符")
        String reason
) {
}
