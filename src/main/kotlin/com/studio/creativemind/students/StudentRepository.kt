package com.studio.creativemind.students

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StudentRepository : JpaRepository<StudentEntity, UUID> {
    fun existsByEmail(email: String): Boolean
}