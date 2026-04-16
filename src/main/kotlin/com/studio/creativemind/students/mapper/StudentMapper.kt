package com.studio.creativemind.students.mapper

import com.studio.creativemind.students.StudentEntity
import com.studio.creativemind.students.dto.CreateStudentRequest
import com.studio.creativemind.students.dto.StudentResponse
import org.springframework.stereotype.Component

@Component
class StudentMapper {

    fun toEntity(request: CreateStudentRequest): StudentEntity {
        return StudentEntity(
            name = request.name,
            email = request.email,
            phone = request.phone,
            birthDate = request.birthDate
        )
    }

    fun toResponse(entity: StudentEntity): StudentResponse {
        return StudentResponse(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
            birthDate = entity.birthDate,
            active = entity.active
        )
    }
}
