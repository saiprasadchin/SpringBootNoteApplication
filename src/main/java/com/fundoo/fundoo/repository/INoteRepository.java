package com.fundoo.fundoo.repository;

import com.fundoo.fundoo.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface INoteRepository extends JpaRepository<Note, Long> {

    @Query(value="select * from note where user_id=:userId", nativeQuery = true)
    Optional<List<Note>> findNoteById(@Param("userId") Long id);
    //@Query(value="select * from note where user_id=:userId",nativeQuery=true)//here native query true indicate :string=true
    //Optional<List<Note>> findNoteById(@Param("userId") Long userId);//@param(string)

    //@Modifying(clearAutomatically = true)
//    @Modifying
//    //@Query(value = "update Note n set n.title= ?1 where n.id= in ?2", nativeQuery = true)
//    @Query(value = "update Note n set n.title= title where n.id=:noteId", nativeQuery = true)
//    int updateNote(@Param("title") String title, @Param("noteId") Long noteId);

    @Modifying
    @Query("UPDATE Note n SET n.title = ?1, n.description = ?2 WHERE n.id = ?3")
    int updateTitleById(String title, String description, Long id);
}
