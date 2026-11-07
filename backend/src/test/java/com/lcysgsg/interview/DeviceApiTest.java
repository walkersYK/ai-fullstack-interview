package com.lcysgsg.interview;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeviceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBeAvailable() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldListSeedDevices() throws Exception {
        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].status").value("NORMAL"));
    }

    @Test
    void shouldCreateDevice() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEV-100\",\"name\":\"测试终端\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DEV-100"))
                .andExpect(jsonPath("$.status").value("NORMAL"));
    }

    @Test
    void shouldRejectBlankDeviceName() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEV-101\",\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectDuplicateDeviceCode() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEV-001\",\"name\":\"重复设备\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldUpdateExistingDevice() throws Exception {
        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"DEV-001\",\"name\":\"主变温度采集器（更新）\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("主变温度采集器（更新）"));
    }

    @Test
    void shouldDisableNormalDevice() throws Exception {
        mockMvc.perform(post("/api/devices/1/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"设备老化，达到使用年限\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"))
                .andExpect(jsonPath("$.disableReason").value("设备老化，达到使用年限"))
                .andExpect(jsonPath("$.disabledAt").exists());
    }

    @Test
    void shouldRejectDisablingDisabledDevice() throws Exception {
        mockMvc.perform(post("/api/devices/1/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"首次停用\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/devices/1/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"重复停用\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldRejectBlankDisableReason() throws Exception {
        mockMvc.perform(post("/api/devices/1/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRejectTooLongDisableReason() throws Exception {
        String longReason = "a".repeat(201);
        mockMvc.perform(post("/api/devices/1/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"" + longReason + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnDisableInfoInListAfterDisabled() throws Exception {
        mockMvc.perform(post("/api/devices/2/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"故障无法修复\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==2)].status").value("DISABLED"))
                .andExpect(jsonPath("$[?(@.id==2)].disableReason").value("故障无法修复"))
                .andExpect(jsonPath("$[?(@.id==2)].disabledAt").exists());
    }
}
