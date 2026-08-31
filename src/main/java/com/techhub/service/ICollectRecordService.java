package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.dto.CollectDTO;
import com.techhub.entity.CollectRecord;
import com.techhub.vo.CollectResultVO;
import jakarta.validation.Valid;

public interface ICollectRecordService extends IService<CollectRecord> {
    CollectResultVO collect(CollectDTO collectDTO);

    CollectResultVO cancleCollect(@Valid CollectDTO collectDTO);
}
