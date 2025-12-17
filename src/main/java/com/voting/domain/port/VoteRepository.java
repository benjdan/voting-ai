/* --------------------------------------------
 * (c) All rights reserved.
 */
package com.voting.domain.port;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.voting.domain.model.Vote;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Vote save(Vote vote);
    @Query("SELECT v FROM Vote v WHERE v.id = :id")
    Optional<Vote> findById(Long id);
    List<Vote> findAll();
    
    @Query("SELECT v FROM Vote v WHERE v.active = true")
    List<Vote> findActiveVotes();
    List<Vote> findByCreatorId(Long creatorId);
}
