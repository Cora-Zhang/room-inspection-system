package com.roominspection.backend.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 值班表导入DTO
 * 用于Excel/CSV导入
 */
@Data
public class ShiftScheduleImportDTO {

    /**
     * 值班日期
     * 格式：yyyy-MM-dd
     */
    @ExcelProperty(value = "值班日期", index = 0)
    private LocalDate scheduleDate;

    /**
     * 班次
     * DAY-白班（08:00-17:00）
     * NIGHT-夜班（18:00-次日07:00）
     */
    @ExcelProperty(value = "班次", index = 1)
    private String shift;

    /**
     * 值班人员工号
     */
    @ExcelProperty(value = "值班人员工号", index = 2)
    private String staffNo;

    /**
     * 值班人员姓名
     */
    @ExcelProperty(value = "值班人员姓名", index = 3)
    private String staffName;

    /**
     * 机房名称
     */
    @ExcelProperty(value = "机房名称", index = 4)
    private String roomName;
}
