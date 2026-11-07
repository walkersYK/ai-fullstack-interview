package com.lcysgsg.interview.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("device")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String status;
    private String disableReason;
    private LocalDateTime disabledAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDisableReason() { return disableReason; }
    public void setDisableReason(String disableReason) { this.disableReason = disableReason; }
    public LocalDateTime getDisabledAt() { return disabledAt; }
    public void setDisabledAt(LocalDateTime disabledAt) { this.disabledAt = disabledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
