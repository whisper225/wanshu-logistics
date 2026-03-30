package com.wanshu.web.repository;

import com.wanshu.model.entity.track.TrackRoute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRouteRepository extends MongoRepository<TrackRoute, String> {

    Optional<TrackRoute> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}
