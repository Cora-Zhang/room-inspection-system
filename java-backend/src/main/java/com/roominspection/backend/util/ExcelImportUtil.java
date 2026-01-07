package com.roominspection.backend.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.roominspection.backend.dto.ShiftScheduleImportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入工具类
 * 用于值班表的Excel导入功能
 */
@Slf4j
@Component
public class ExcelImportUtil {

    /**
     * 解析值班表Excel文件
     *
     * @param file Excel文件
     * @return 值班排班数据列表
     */
    public List<ShiftScheduleImportDTO> parseShiftScheduleExcel(MultipartFile file) throws IOException {
        List<ShiftScheduleImportDTO> dataList = new ArrayList<>();

        EasyExcel.read(file.getInputStream(), ShiftScheduleImportDTO.class,
                new AnalysisEventListener<ShiftScheduleImportDTO>() {
                    @Override
                    public void invoke(ShiftScheduleImportDTO data, AnalysisContext context) {
                        // 数据校验
                        if (isValidData(data)) {
                            dataList.add(data);
                        }
                    }

                    @Override
                    public void doAfterAllAnalysed(AnalysisContext context) {
                        log.info("Excel解析完成，共读取{}条有效数据", dataList.size());
                    }
                }).sheet().doRead();

        return dataList;
    }

    /**
     * 解析值班表CSV文件
     *
     * @param file CSV文件
     * @return 值班排班数据列表
     */
    public List<ShiftScheduleImportDTO> parseShiftScheduleCsv(MultipartFile file) throws IOException {
        List<ShiftScheduleImportDTO> dataList = new ArrayList<>();

        // CSV格式：日期,班次,值班人员工号,值班人员姓名,机房名称
        List<String> lines = new ArrayList<>(file.getInputStream().readAllBytes())
                .stream()
                .map(bytes -> new String(bytes))
                .toList();

        // 跳过表头
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = line.split(",");

            if (fields.length >= 5) {
                ShiftScheduleImportDTO dto = new ShiftScheduleImportDTO();
                dto.setScheduleDate(LocalDate.parse(fields[0].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dto.setShift(fields[1].trim());
                dto.setStaffNo(fields[2].trim());
                dto.setStaffName(fields[3].trim());
                dto.setRoomName(fields[4].trim());

                if (isValidData(dto)) {
                    dataList.add(dto);
                }
            }
        }

        log.info("CSV解析完成，共读取{}条有效数据", dataList.size());
        return dataList;
    }

    /**
     * 数据校验
     *
     * @param data 待校验数据
     * @return 是否有效
     */
    private boolean isValidData(ShiftScheduleImportDTO data) {
        if (data.getScheduleDate() == null) {
            log.warn("值班日期为空，跳过该条数据");
            return false;
        }

        if (data.getShift() == null || (!"DAY".equals(data.getShift()) && !"NIGHT".equals(data.getShift()))) {
            log.warn("班次无效：{}，跳过该条数据", data.getShift());
            return false;
        }

        if (data.getStaffNo() == null || data.getStaffNo().trim().isEmpty()) {
            log.warn("值班人员工号为空，跳过该条数据");
            return false;
        }

        if (data.getStaffName() == null || data.getStaffName().trim().isEmpty()) {
            log.warn("值班人员姓名为空，跳过该条数据");
            return false;
        }

        if (data.getRoomName() == null || data.getRoomName().trim().isEmpty()) {
            log.warn("机房名称为空，跳过该条数据");
            return false;
        }

        return true;
    }

    /**
     * 生成值班表Excel模板
     *
     * @return Excel文件字节数组
     */
    public byte[] generateShiftScheduleTemplate() {
        List<ShiftScheduleImportDTO> templateData = new ArrayList<>();

        // 示例数据
        ShiftScheduleImportDTO example = new ShiftScheduleImportDTO();
        example.setScheduleDate(LocalDate.now());
        example.setShift("DAY");
        example.setStaffNo("ST001");
        example.setStaffName("张三");
        example.setRoomName("中心机房");
        templateData.add(example);

        return EasyExcel.write()
                .head(ShiftScheduleImportDTO.class)
                .sheet("值班表模板")
                .doWrite(templateData);
    }
}
