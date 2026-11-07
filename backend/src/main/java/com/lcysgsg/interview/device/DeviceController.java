package com.lcysgsg.interview.device;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<Device> list() {
        return deviceService.list();
    }

    @PostMapping
    public Device create(@Valid @RequestBody DeviceRequest request) {
        return deviceService.create(request);
    }

    @PutMapping("/{id}")
    public Device update(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        return deviceService.update(id, request);
    }

    @PostMapping("/{id}/disable")
    public Device disable(@PathVariable Long id, @Valid @RequestBody DeviceDisableRequest request) {
        return deviceService.disable(id, request);
    }
}
