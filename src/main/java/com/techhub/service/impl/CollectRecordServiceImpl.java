package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.entity.CollectRecord;
import com.techhub.mapper.CollectRecordMapper;
import com.techhub.service.ICollectRecordService;
import org.springframework.stereotype.Service;

@Service
public class CollectRecordServiceImpl extends ServiceImpl<CollectRecordMapper, CollectRecord> implements ICollectRecordService {
}
