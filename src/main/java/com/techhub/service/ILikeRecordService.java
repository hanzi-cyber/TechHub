package com.techhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.techhub.dto.LikeDTO;
import com.techhub.entity.LikeRecord;
import com.techhub.vo.LikeResultVO;

public interface ILikeRecordService extends IService<LikeRecord> {

    /**
     * 点赞(幂等),返回操作后的点赞状态与数量
     */
    LikeResultVO like(LikeDTO likeDTO);

    /**
     * 取消点赞(幂等),返回操作后的点赞状态与数量
     */
    LikeResultVO unlike(LikeDTO likeDTO);
}
