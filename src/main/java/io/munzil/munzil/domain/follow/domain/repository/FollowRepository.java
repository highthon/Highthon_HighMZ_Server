package io.munzil.munzil.domain.follow.domain.repository;

import io.munzil.munzil.domain.follow.domain.Follow;
import io.munzil.munzil.domain.follow.domain.FollowId;
import io.munzil.munzil.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {

    @Query("select f from Follow f join fetch f.targetUser where f.user=:user")
    List<Follow> findAllByUserWithTarget(@Param("user") User user);

    @Query("select f from Follow f join fetch f.user where f.targetUser=:target")
    List<Follow> findAllByTargetUserWithUser(@Param("target")  User targetUser);

    Optional<Follow> findByUserAndTargetUser(User user, User targetUser);
}
