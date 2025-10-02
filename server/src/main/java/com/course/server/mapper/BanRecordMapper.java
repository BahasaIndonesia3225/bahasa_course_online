//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.course.server.mapper;

import com.course.server.domain.BanRecord;
import java.util.List;

public interface BanRecordMapper {
    int insertSelective(BanRecord record);

    List<BanRecord> selectBanRecord(BanRecord record);

    long selectBanRecordCount(BanRecord record);
}
