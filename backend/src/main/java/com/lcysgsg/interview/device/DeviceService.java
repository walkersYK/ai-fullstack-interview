package com.lcysgsg.interview.device;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {
    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_DISABLED = "DISABLED";

    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceMapper deviceMapper) {
        this.deviceMapper = deviceMapper;
    }

    @Transactional(readOnly = true)
    public List<Device> list() {
        return deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().orderByDesc(Device::getId)
        );
    }

    @Transactional
    public Device create(DeviceRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        ensureCodeAvailable(code, null);

        Device device = new Device();
        device.setCode(code);
        device.setName(name);
        device.setStatus(STATUS_NORMAL);
        device.setCreatedAt(LocalDateTime.now());
        deviceMapper.insert(device);
        return device;
    }

    @Transactional
    public Device update(Long id, DeviceRequest request) {
        Device device = requireDevice(id);
        String code = request.code().trim();
        String name = request.name().trim();
        ensureCodeAvailable(code, id);

        device.setCode(code);
        device.setName(name);
        deviceMapper.updateById(device);
        return device;
    }

    @Transactional
    public Device disable(Long id, DeviceDisableRequest request) {
        Device device = requireDevice(id);
        if (!STATUS_NORMAL.equals(device.getStatus())) {
            throw new IllegalArgumentException("仅正常状态的设备可以停用");
        }

        String reason = request.reason().trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("停用原因不能为空");
        }

        device.setStatus(STATUS_DISABLED);
        device.setDisableReason(reason);
        device.setDisabledAt(LocalDateTime.now());
        deviceMapper.updateById(device);
        return device;
    }

    private Device requireDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        return device;
    }

    private void ensureCodeAvailable(String code, Long excludeId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<Device>()
                .eq(Device::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Device::getId, excludeId);
        }
        if (deviceMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("设备编号已存在");
        }
    }
}
