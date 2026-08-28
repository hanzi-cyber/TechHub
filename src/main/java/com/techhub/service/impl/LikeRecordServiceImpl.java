package com.techhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.techhub.entity.LikeRecord;
import com.techhub.mapper.LikeRecordMapper;
import com.techhub.service.ILikeRecordService;
import org.springframework.stereotype.Service;

@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {
}
