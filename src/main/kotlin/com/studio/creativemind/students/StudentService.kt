package com.studio.creativemind.students

import com.studio.creativemind.common.BusinessException
import com.studio.creativemind.students.dto.CreateStudentRequest
import com.studio.creativemind.students.dto.StudentResponse
import com.studio.creativemind.students.mapper.StudentMapper
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val mapper: StudentMapper
) {

    fun create(request: CreateStudentRequest): StudentResponse {
        if (studentRepository.existsByEmail(request.email)) {
            throw BusinessException("Email already exists")
        }
        val savedStudent = studentRepository.save(mapper.toEntity(request))
        return mapper.toResponse(savedStudent)
    }

    fun findById(id: UUID): StudentResponse {
        val student = studentRepository.findById(id)
            .orElseThrow { throw BusinessException("Student not found") }

        return mapper.toResponse(student)
    }
}