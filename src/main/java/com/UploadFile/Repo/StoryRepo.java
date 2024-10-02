package com.UploadFile.Repo;

import com.UploadFile.Entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepo extends JpaRepository<Story ,Long> {
}
