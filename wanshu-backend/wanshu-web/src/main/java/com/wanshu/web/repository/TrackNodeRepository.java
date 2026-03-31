package com.wanshu.web.repository;

import com.wanshu.model.entity.track.TrackNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackNodeRepository extends MongoRepository<TrackNode, String> {

    /** 按运单 ID 查询所有跟踪节点，按时间降序（最新在前） */
    List<TrackNode> findByWaybillIdOrderByTimeDesc(Long waybillId);
}
