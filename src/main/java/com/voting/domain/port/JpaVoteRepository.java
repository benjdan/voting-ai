/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.port;

import com.voting.domain.model.Vote;

import lombok.AllArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaVoteRepository extends JpaRepository<Vote, Long> {
    
    @Query("SELECT v FROM Vote v WHERE v.active = true AND v.endDate > CURRENT_TIMESTAMP")
    List<Vote> findActiveVotes();
    
    List<Vote> findByCreatorId(Long creatorId);
}
