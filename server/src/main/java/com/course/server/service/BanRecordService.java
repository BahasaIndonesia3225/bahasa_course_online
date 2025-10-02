//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.course.server.service;

import com.course.server.domain.BanRecord;
import com.course.server.dto.PageDto;
import com.course.server.mapper.BanRecordMapper;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BanRecordService {
    @Resource
    private BanRecordMapper BanRecordMapper;

    public int insert(BanRecord record) {
        return this.BanRecordMapper.insertSelective(record);
    }

    public PageDto selectBanRecords(BanRecord record) {
        PageDto pageDto = new PageDto();
        List<BanRecord> banRecords = this.BanRecordMapper.selectBanRecord(record);
        pageDto.setList(banRecords);
        pageDto.setTotal(this.BanRecordMapper.selectBanRecordCount(record));
        pageDto.setPage(record.getPage());
        pageDto.setSize(record.getSize());
        return pageDto;
    }
}
